package com;

import com.google.gson.JsonObject;
import core.ApiRequest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.*;
import io.netty.util.CharsetUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class ApiRequestParser extends SimpleChannelInboundHandler<FullHttpMessage> {

    private static final Logger logger =  LogManager.getLogger(ApiRequestParser.class);

    private HttpRequest request;
    private JsonObject apiResult;

    private static final HttpDataFactory factory =
            new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);

    private HttpPostRequestDecoder decoder;

    private Map<String, String> reqData = new HashMap<String, String>();

    private static final Set<String> usingHeader = new HashSet<String>();
    static {
        usingHeader.add("token");
        usingHeader.add("email");
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx){
        logger.info("요청 처리 완료");
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpMessage msg){ // 1
        // Request header 처리
        if (msg instanceof HttpRequest) {   // 2
            this.request = (HttpRequest) msg;   // 3

            if(HttpHeaders.is100ContinueExpected(request)){
                send100Continue(ctx);
            }

            HttpHeaders headers = request.headers();    //4
            if(!headers.isEmpty()) {
                for(Map.Entry<String, String> h : headers) {
                    String key = h.getKey();
                    if(usingHeader.contains(key)) { // 5
                        reqData.put(key, h.getValue()); // 6
                    }
                }
            }

            reqData.put("REQUEST_URI", request.getUri());   // 7
            reqData.put("REQUEST_METTHOD", request.getMethod().name()); //8
        }

        if (msg instanceof HttpContent) {   // 9
            HttpContent httpContent = (HttpContent) msg;    // 10

            ByteBuf content = httpContent.content();    // 11

            if(msg instanceof LastHttpContent) {    // 12
                logger.info("LastHttpContent message received!!" + request.getUri());

                LastHttpContent trailer = (LastHttpContent)msg;

                readPostData(); // 13

                ApiRequest service = ServiceDispatcher.dispatch(reqData);   // 14

                try {
                    service.executeService();   // 15
                    apiResult = service.getApiResult(); // 16
                } finally {
                    reqData.clear();
                }

                if(!writeResponse(trailer, ctx)) {  // 17
                    ctx.writeAndFlush(Unpooled.EMPTY_BUFFER)
                            .addListener(ChannelFutureListener.CLOSE);
                }

                reset();
            }
        }
    }

    private void reset() {
        request = null;
    }


    private void readPostData() {
        try {
            decoder = new HttpPostRequestDecoder(factory, request); // 1
            for(InterfaceHttpData data : decoder.getBodyHttpDatas()) {
                if (InterfaceHttpData.HttpDataType.Attribute == data.getHttpDataType()) {
                    try {
                        Attribute attribute = (Attribute) data; // 2
                        reqData.put(attribute.getName(), attribute.getValue()); // 3
                    } catch (IOException e) {
                        logger.error("BODY Attribute: " + data.getHttpDataType().name(), e);
                        return;
                    }
                } else {
                    logger.info("BODY data: " + data.getHttpDataType().name() + ": " + data);
                }
            }
        } catch (HttpPostRequestDecoder.ErrorDataDecoderException e) {
            logger.error(e);
        } finally {
            if(decoder != null) {
                decoder.destroy();
            }
        }
    }

    private boolean writeResponse(HttpObject currentObj, ChannelHandlerContext ctx) {
        // Decide whether to close the connection or not.
        boolean keepAlive = HttpHeaders.isKeepAlive(request);
        // Build the response object.
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
                currentObj.getDecoderResult().isSuccess() ? OK : BAD_REQUEST, Unpooled.copiedBuffer(
                apiResult.toString(), CharsetUtil.UTF_8));

        response.headers().set(CONTENT_TYPE, "application/json; charset=UTF-8");

        if (keepAlive) {
            // Add 'Content-Length' header only for a keep-alive connection.
            response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
            // Add keep alive header as per:
            // -
            // http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Connection
            response.headers().set(CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        }

        // Write the response.
        ctx.write(response);

        return keepAlive;
    }


    private static void send100Continue(ChannelHandlerContext ctx){
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, CONTINUE);
        ctx.write(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error(cause);
        ctx.close();
    }
}
