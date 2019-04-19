package com.sample.QiitaViewsCountApi_Sample;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import okhttp3.OkHttpClient;

public class Main {
    @FunctionName("view")
    public HttpResponseMessage main(
            @HttpTrigger(name = "req",
        	methods = {HttpMethod.GET},
        	authLevel = AuthorizationLevel.ANONYMOUS)
        HttpRequestMessage<Optional<String>> request,
        ExecutionContext context) throws IOException {

    	context.getLogger().info("Java HTTP trigger processed a request.");

		String queryToken = request.getQueryParameters().get("token");
		String token = request.getBody().orElse(queryToken);

		if (StringUtils.isEmpty(token)) {
			return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
					.body("Please pass a token on the query string or in the request body")
					.build();
		}

		OkHttpClient.Builder builder = new OkHttpClient.Builder();
		builder.connectTimeout(180, TimeUnit.SECONDS)
				.writeTimeout(180, TimeUnit.SECONDS)
				.readTimeout(180, TimeUnit.SECONDS);

		ProxySetter.proxySet(builder);

		OkHttpClient client = builder.build();

		context.getLogger().info("Start getUserItemsCount");
		int itemCount = QiitaItemGetter.getUserItemsCount(client, token);
		int loopCount = (itemCount / 100) + 1;

		context.getLogger().info("Start getItemIdList");
		ArrayList<String> list = new ArrayList<>();
		list = QiitaItemGetter.getItemIdList(client, token, loopCount);

		context.getLogger().info("Start getItemDetails");
		String body = QiitaItemGetter.getItemDetails(client, token, list);

        return request.createResponseBuilder(HttpStatus.OK).body(body).build();
    }
}
