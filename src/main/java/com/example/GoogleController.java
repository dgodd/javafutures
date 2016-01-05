package com.example;

import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureAdapter;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Controller
public class GoogleController {
//    @Autowired
//    AsyncRestTemplate restTemplate;

    @RequestMapping("/hidad/{uname}")
    public Issue[] hidad(@PathVariable("uname") String uname) throws ExecutionException, InterruptedException {
        AsyncRestTemplate asyncTemp = new AsyncRestTemplate();
        asyncTemp.setErrorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                return false;
            }

            @Override
            public void handleError(ClientHttpResponse response) throws IOException {

            }
        });

        String url = "https://api.github.com/users/{uname}/gists";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<String>("params", headers);
        ListenableFuture<ResponseEntity<Issue[]>> future = asyncTemp.exchange(url, HttpMethod.GET, requestEntity, Issue[].class, uname);

        return future.get().getBody();
    }


    @RequestMapping("/himom/{uname}")
    public ListenableFuture<List<Issue>> himom(@PathVariable("uname") String uname) {
        AsyncRestTemplate asyncTemp = new AsyncRestTemplate();
        asyncTemp.setErrorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                return response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError();
            }

            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                HttpStatus statusCode = response.getStatusCode();
                switch (statusCode.series()) {
                    case CLIENT_ERROR:
                        return;
                    case SERVER_ERROR:
                        throw new HttpServerErrorException(statusCode, response.getStatusText(),
                                response.getHeaders(), "".getBytes(), Charset.defaultCharset());
                    default:
                        throw new RestClientException("Unknown status code [" + statusCode + "]");
                }
            }
        });

        String url = "https://api.github.com/users/{uname}/gists";
        url = url.replace("{uname}", uname);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<String>("params", headers);

        ListenableFuture<ResponseEntity<Issue[]>> future1 = asyncTemp.exchange(url, HttpMethod.GET, requestEntity, Issue[].class, uname);
        ListenableFuture<ResponseEntity<Issue[]>> future2 = asyncTemp.exchange(url, HttpMethod.GET, requestEntity, Issue[].class, uname);

        ListenableFutureAdapter<List<Issue>, ResponseEntity<Issue[]>> adapter = new ListenableFutureAdapter<List<Issue>, ResponseEntity<Issue[]>>(future1) {
            @Override
            protected List<Issue> adapt(ResponseEntity<Issue[]> adapteeResult) throws ExecutionException {
                return null;
            }
        };


//        com.spotify.trickle.Func2<String, String, String> combineInputs = new com.spotify.trickle.Func2<String, String, String>() {
//            @Override
//            public ListenableFuture<String> run(String arg1, String arg2) {
//                System.out.println(" - combining inputs");
//                return immediateFuture(arg1 + " " + arg2);
//            }
//        };

        return adapter;

    }
}
