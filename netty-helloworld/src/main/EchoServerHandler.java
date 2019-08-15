import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.charset.Charset;

/**
 * Created by red on 2015. 12. 25..
 */
public class EchoServerHandler extends ChannelInboundHandlerAdapter {
  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    String readMessage = ((ByteBuf) msg).toString(Charset.defaultCharset());

    System.out.println("수신한 문자열 [" + readMessage + "]");

    ctx.write(msg);
  }

  @Override
  public void channelReadComplete(ChannelHandlerContext ctx){
    ctx.flush();
  }
  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
    cause.printStackTrace();
    ctx.close();
  }
}
