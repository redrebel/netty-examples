package service;

import com.google.gson.JsonObject;
import core.ApiRequestTemplate;
import core.JedisHelper;
import core.KeyMaker;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;
import service.dao.TokenKey;

import java.util.Map;

@Service("tokenIssue")  // 1. getBean의 호출인자로 사용된다.
@Scope("prototype")
public class TokenIssue extends ApiRequestTemplate {
    private static final JedisHelper helper = JedisHelper.getInstance();

    @Autowired
    private SqlSession sqlSession;

    public TokenIssue(Map<String, String> reqData) {
        super(reqData);
    }

    public void requestParamValidation() throws RequestParamException { // 3
        if(StringUtils.isEmpty(this.reqData.get("userNo"))) {
            throw new RequestParamException("userNo이 없습니다.");
        }

        if(StringUtils.isEmpty(this.reqData.get("passwrod"))) {
            throw new RequestParamException("password가 없습니다.");
        }
    }

    public void service() throws ServiceException {
        Jedis jedis = null;

        try {
            Map<String, Object> result = sqlSession.selectOne(
                    "users.userInfoByPassword", this.reqData);
            if(result != null) {
                // 교제에서는 threeHour가 Long형이다.
                final int threeHour = 60 * 60 * 3;
                long issueDate = System.currentTimeMillis() / 1000;
                String email = String.valueOf(result.get("USERID"));

                JsonObject token = new JsonObject();
                token.addProperty("issueDate", issueDate);
                token.addProperty("expireDate", issueDate + threeHour);
                token.addProperty("email", email);
                token.addProperty("userNo", reqData.get("userNo"));

                // token 저장
                KeyMaker tokenKey = new TokenKey(email, issueDate); // 4
                jedis = helper.getConnection();

                // 지정된 시간 이후에 데이터를 자동으로 삭제
                jedis.setex(tokenKey.getKey(), threeHour, token.toString());

                // helper
                this.apiResult.addProperty("resultCode", "200");
                this.apiResult.addProperty("message", "Success");
                this.apiResult.addProperty("token", tokenKey.getKey());
            } else {
                // 데이터 없음
                this.apiResult.addProperty("resultCode", "404");
            }

            helper.returnResource(jedis);
        } catch (Exception e) {
            helper.returnResource(jedis);
        }
    }

}
