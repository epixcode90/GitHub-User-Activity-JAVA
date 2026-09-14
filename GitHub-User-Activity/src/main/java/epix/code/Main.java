package epix.code;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        Scanner input = new Scanner(System.in);


        //input username
        System.out.print("github-activity ");
        String username = input.nextLine();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://api.github.com/users/"+username+"/events"))
                    .GET()
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            Gson gson = new GsonBuilder().setPrettyPrinting().create();

//            String output = gson.toJson(
//                    gson.fromJson(response.body(), Object.class)
//            );

            Event[] events = gson.fromJson(response.body(), Event[].class);

            for (Event event : events) {

                if ("PushEvent".equals(event.getType())) {

                    String repoName = event.getRepo().name;
                    String before = event.getPayload().before;
                    String head = event.getPayload().head;

                    String compareUrl =
                            "https://api.github.com/repos/" +
                                    repoName +
                                    "/compare/" +
                                    before +
                                    "..." +
                                    head;

                    HttpRequest compareRequest = HttpRequest.newBuilder()
                            .uri(new URI(compareUrl))
                            .GET()
                            .build();

                    HttpResponse<String> compareResponse =
                            client.send(
                                    compareRequest,
                                    HttpResponse.BodyHandlers.ofString()
                            );

                    CompareResult result =
                            gson.fromJson(
                                    compareResponse.body(),
                                    CompareResult.class
                            );

                    System.out.println(
                            "- Pushed " +
                                    result.total_commits +
                                    " commits to " +
                                    repoName
                    );
                }
            }

//            System.out.println(output);

        }catch (Exception e){
            System.err.println(e.getMessage());
        }




    }
}
