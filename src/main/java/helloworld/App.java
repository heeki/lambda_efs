package helloworld;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;

public class App implements RequestHandler<Map<String, String>, Map<String, String>> {

    public Map<String, String> handleRequest(final Map<String, String> event, final Context context) {
        Gson gson = new Gson(); 
        Map<String, String> response = new HashMap<>();
        response.put("envvar1", System.getenv("ENVVAR1"));
        response.put("envvar2", System.getenv("ENVVAR2"));
        response.put("region", System.getenv("AWS_REGION"));
        response.put("execution_env", System.getenv("AWS_EXECUTION_ENV"));
        response.put("initialization_type", System.getenv("AWS_LAMBDA_INITIALIZATION_TYPE"));
        try {
            // perform some business logic
            final String pageContents = this.getPageContents("https://checkip.amazonaws.com");
            response.put("ip_address", String.format("%s", pageContents));

            // determine write location: /tmp if local, /mnt/efs if in aws
            String location = (System.getenv("AWS_LAMBDA_INITIALIZATION_TYPE") == null) ? "/tmp" : "/mnt/efs";
            response.put("filesystem_location", location);
            String file = String.format("%s/%s", location, "test.json");
            String json = gson.toJson(response);

            // write to location, read from location to verify
            writeFile(file, json);
            System.out.println(readFile(file));

            return response;
        } catch (IOException e) {
            return response;
        }
    }

    private String getPageContents(String address) throws IOException{
        URL url = new URL(address);
        try(BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()))) {
            return br.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }

    private void writeFile(String file, String payload) {
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
            bw.write(payload);
            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String readFile(String file) {
        String verify = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            verify = br.lines().collect(Collectors.joining(System.lineSeparator()));
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return verify;
    }
}
