// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.sample.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.microsoft.bot.ai.luis.LuisApplication;
import com.microsoft.bot.ai.luis.LuisRecognizer;
import com.microsoft.bot.ai.luis.LuisRecognizerOptionsV3;
import com.microsoft.bot.builder.Recognizer;
import com.microsoft.bot.builder.RecognizerResult;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.integration.Configuration;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.CompletableFuture;

/**
 * The class in charge of recognizing the booking information.
 */
public class EntityRecognizer implements Recognizer {

    private LuisRecognizer recognizer;

    public EntityRecognizer(Configuration configuration) {
        Boolean luisIsConfigured = StringUtils.isNotBlank(configuration.getProperty("LuisAppId")) && StringUtils.isNotBlank(configuration.getProperty("LuisAPIKey")) && StringUtils.isNotBlank(configuration.getProperty("LuisAPIHostName"));
        if (luisIsConfigured) {
            LuisApplication luisApplication = new LuisApplication(
                configuration.getProperty("LuisAppId"),
                configuration.getProperty("LuisAPIKey"),
                String.format("https://%s", configuration.getProperty("LuisAPIHostName"))
            );
            LuisRecognizerOptionsV3 recognizerOptions = new LuisRecognizerOptionsV3(luisApplication);
            recognizerOptions.setIncludeInstanceData(true);
            this.recognizer = new LuisRecognizer(recognizerOptions);
        }
    }


    public Boolean isConfigured() {
        return this.recognizer != null;
    }


    public CompletableFuture<RecognizerResult> executeLuisQuery(TurnContext context) {
        return this.recognizer.recognize(context);
    }


    public ObjectNode getFromEntities(RecognizerResult result) {
        String fromValue = "", fromAirportValue = "";
        if (result.getEntities().get("$instance").get("From") != null) {
            fromValue = result.getEntities().get("$instance").get("From").get(0).get("text")
                .asText();
        }
        if (!fromValue.isEmpty()
            && result.getEntities().get("From").get(0).get("Airport") != null) {
            fromAirportValue = result.getEntities().get("From").get(0).get("Airport").get(0).get(0)
                .asText();
        }

        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        ObjectNode entitiesNode = mapper.createObjectNode();
        entitiesNode.put("from", fromValue);
        entitiesNode.put("airport", fromAirportValue);
        return entitiesNode;
    }


    public ObjectNode getToEntities(RecognizerResult result) {
        String toValue = "", toAirportValue = "";
        if (result.getEntities().get("$instance").get("To") != null) {
            toValue = result.getEntities().get("$instance").get("To").get(0).get("text").asText();
        }
        if (!toValue.isEmpty() && result.getEntities().get("To").get(0).get("Airport") != null) {
            toAirportValue = result.getEntities().get("To").get(0).get("Airport").get(0).get(0)
                .asText();
        }

        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        ObjectNode entitiesNode = mapper.createObjectNode();
        entitiesNode.put("to", toValue);
        entitiesNode.put("airport", toAirportValue);
        return entitiesNode;
    }

    public ObjectNode getDirectionEntities(RecognizerResult result){
        String directionValue="";
        if(result.getEntities().get("$instance").get("direction")!=null){
            directionValue=result.getEntities().get("$instance").get("direction").get(0).get("text").asText();
        }
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        ObjectNode entitiesNode = mapper.createObjectNode();
        entitiesNode.put("direction", directionValue);
        return entitiesNode;
    }

    /**
     * Runs an utterance through a recognizer and returns a generic recognizer result.
     *
     * @param turnContext Turn context.
     * @return Analysis of utterance.
     */
    @Override
    public CompletableFuture<RecognizerResult> recognize(TurnContext turnContext) {
        return this.recognizer.recognize(turnContext);
    }
}
