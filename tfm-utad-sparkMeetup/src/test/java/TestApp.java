import com.tfm.utad.sparkMeetup.HttpResolver;
import org.json.simple.JSONObject;


public class TestApp {

    public static void main(String[] args) throws Exception {
        try {

            final HttpResolver resolver = new HttpResolver();
            System.out.println("Test start ");
            JSONObject group = resolver.getGroup("312598");
            System.out.println("group: " + group);
            System.out.println("Test end");


        } catch (Exception ex) {
            System.err.println("Exception: " + ex.getMessage());
        }
    }
}

