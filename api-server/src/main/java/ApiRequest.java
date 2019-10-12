import com.google.gson.JsonObject;
import service.RequestParamException;
import service.ServiceException;

public interface ApiRequest {
    public void requestParamValidation() throws RequestParamException;

    public void service() throws ServiceException;

    public void executeService();

    public JsonObject getApiResult();
}
