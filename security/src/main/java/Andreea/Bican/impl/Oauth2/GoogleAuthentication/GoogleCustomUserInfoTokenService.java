package Andreea.Bican.impl.Oauth2.GoogleAuthentication;

import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import java.util.List;
import java.util.Map;

import static org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes;

/**
 * Created by andre on 13.06.2016.
 */
public class GoogleCustomUserInfoTokenService extends UserInfoTokenServices {


    public GoogleCustomUserInfoTokenService(String userInfoEndpointUrl, String clientId)
    {
        super(userInfoEndpointUrl, clientId);
    }

    public OAuth2Authentication loadAuthentication(String accessToken) throws AuthenticationException
    {
        OAuth2Authentication auth = super.loadAuthentication(accessToken);
        if (userIsKnown(getUserDetails(auth))) {
            getUserDetails(auth).put("userId", getUserId(getUserDetails(auth)));
            getUserDetails(auth).put("userEmail", getUserEmail(getUserDetails(auth)));
            getUserDetails(auth).put("userName", getUserName(getUserDetails(auth)));
            getUserDetails(auth).put("token", accessToken);
            getUserDetails(auth).put("provider", "Google");
            getUserDetails(auth).put("JSessionId", getJSessionId());
        }
        else {
            throw new UsernameNotFoundException("Unknown user: " + getUserEmail(getUserDetails(auth)));
        }

        return auth;
    }


    private boolean userIsKnown(Map<String, Object> userDetails)
    {
        String email = getUserEmail(userDetails);
        if (email.equals("unknown")) return false;
        // look in db here
        return true;
    }

    private String getUserName(Map<String, Object> userDetails)
    {
        if(userDetails.containsKey("displayName")){
            String userName = (String) userDetails.get("displayName");
            return userName;
        }
        return "unknown";
    }

    private String getUserEmail(Map<String, Object> userDetails)
    {
        if (userDetails.containsKey("emails")) {
            List<Map<String, Object>> emails =
                    (List<Map<String, Object>>) userDetails.get("emails");
            for (Map<String, Object> emailAccount : emails) {
                if (emailAccount.containsKey("value")) {
                    String email = (String) emailAccount.get("value");
                    return email;
                }
            }
        }
        return "unknown";
    }

    private String getJSessionId(){
        String jsessionId = currentRequestAttributes().getSessionId();
        if(jsessionId == null){
            return "No session id";
        }else{
            return jsessionId;
        }
    }

    private String getUserId(Map<String, Object> userDetails)
    {
        if(userDetails.containsKey("id"))
        {
            return (String)userDetails.get("id");
        }
        return "unknown";
    }

    private Map<String, Object> getUserDetails(OAuth2Authentication auth)
    {
        return (Map<String, Object>) auth.getUserAuthentication().getDetails();
    }
}
