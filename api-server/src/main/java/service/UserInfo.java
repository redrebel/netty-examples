package service;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import core.ApiRequestTemplate;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * 사용자번호 조회 API
 */
@Service("users") // 1
@Scope("prototype") // 2. 요청시마다 새로 생긴다.
public class UserInfo extends ApiRequestTemplate{
    @Autowired
    private SqlSession sqlSession;  // 3

    public UserInfo(Map<String, String> reqData) {
        super(reqData);
    }   // 4

    @Override
    public void requestParamValidation() throws RequestParamException { // 5
        if(StringUtils.isEmpty(this.reqData.get("email"))) {
            throw new RequestParamException("email이 없습니다.");
        }
    }

    @Override
    public void service() throws ServiceException {
        Map<String, Object> result =
                sqlSession.selectOne("users.userInfoByEmail", this.reqData);    // 6

        if(result != null) {
            String userNo = String.valueOf(result.get("USERNO"));

            this.apiResult.addProperty("resultCode", "200");    // 7
            this.apiResult.addProperty("message", "Success");
            this.apiResult.addProperty("userNo", userNo);
        }
        else {
            this.apiResult.addProperty("resultCode", "404");
            this.apiResult.addProperty("message", "Fail");

        }
    }
}
