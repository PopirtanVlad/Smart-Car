// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.bot.sample.core;

import com.microsoft.bot.builder.ActivityHandler;
import com.microsoft.bot.builder.BotState;
import com.microsoft.bot.builder.ConversationState;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.builder.UserState;
import com.microsoft.bot.dialogs.Dialog;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public class DialogBot<T extends Dialog> extends ActivityHandler {

    private Dialog dialog;
    private BotState conversationState;
    private BotState userState;


    protected Dialog getDialog() {
        return dialog;
    }

    protected BotState getConversationState() {
        return conversationState;
    }


    protected BotState getUserState() {
        return userState;
    }

    protected void setDialog(Dialog withDialog) {
        dialog = withDialog;
    }


    protected void setConversationState(BotState withConversationState) {
        conversationState = withConversationState;
    }

    protected void setUserState(BotState withUserState) {
        userState = withUserState;
    }


    public DialogBot(
        ConversationState withConversationState, UserState withUserState, T withDialog
    ) {
        this.conversationState = withConversationState;
        this.userState = withUserState;
        this.dialog = withDialog;
    }


    @Override
    public CompletableFuture<Void> onTurn(TurnContext turnContext) {
        return super.onTurn(turnContext)
            // Save any state changes that might have occurred during the turn.
            .thenCompose(turnResult -> conversationState.saveChanges(turnContext, false))
            .thenCompose(saveResult -> userState.saveChanges(turnContext, false));
    }


    @Override
    protected CompletableFuture<Void> onMessageActivity(TurnContext turnContext) {
        LoggerFactory.getLogger(DialogBot.class).info("Running dialog with Message Activity.");

        // Run the Dialog with the new message Activity.
        return Dialog.run(dialog, turnContext, conversationState.createProperty("DialogState"));
    }
}
