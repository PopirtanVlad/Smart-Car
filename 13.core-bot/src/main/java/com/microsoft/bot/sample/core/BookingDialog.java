package com.microsoft.bot.sample.core;

import com.microsoft.bot.builder.MessageFactory;
import com.microsoft.bot.dialogs.DialogTurnResult;
import com.microsoft.bot.dialogs.WaterfallDialog;
import com.microsoft.bot.dialogs.WaterfallStep;
import com.microsoft.bot.dialogs.WaterfallStepContext;
import com.microsoft.bot.dialogs.prompts.ConfirmPrompt;
import com.microsoft.bot.dialogs.prompts.PromptOptions;
import com.microsoft.bot.dialogs.prompts.TextPrompt;
import com.microsoft.bot.sample.core.Entities.BookingDetails;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.InputHints;
import com.microsoft.recognizers.datatypes.timex.expression.Constants;
import com.microsoft.recognizers.datatypes.timex.expression.TimexProperty;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;


public class BookingDialog extends CancelAndHelpDialog {

    private final String destinationStepMsgText = "Where would you like to travel to?";
    private final String originStepMsgText = "Where are you traveling from?";

    /**
     * The constructor of the Booking Dialog class.
     */
    public BookingDialog() {
        super("BookingDialog");

        addDialog(new TextPrompt("TextPrompt"));
        addDialog(new ConfirmPrompt("ConfirmPrompt"));
        WaterfallStep[] waterfallSteps = {
            this::destinationStep,
            this::originStep,
            this::confirmStep,
            this::finalStep
        };
        addDialog(new WaterfallDialog("WaterfallDialog", Arrays.asList(waterfallSteps)));

        // The initial child Dialog to run.
        setInitialDialogId("WaterfallDialog");
    }


    private CompletableFuture<DialogTurnResult> destinationStep(WaterfallStepContext stepContext) {
        BookingDetails bookingDetails = (BookingDetails) stepContext.getOptions();

        if (bookingDetails.getDestination().isEmpty()) {
            Activity promptMessage =
                MessageFactory.text(destinationStepMsgText, destinationStepMsgText,
                    InputHints.EXPECTING_INPUT
                );
            PromptOptions promptOptions = new PromptOptions();
            promptOptions.setPrompt(promptMessage);
            return stepContext.prompt("TextPrompt", promptOptions);
        }

        return stepContext.next(bookingDetails.getDestination());
    }


    private CompletableFuture<DialogTurnResult> originStep(WaterfallStepContext stepContext) {
        BookingDetails bookingDetails = (BookingDetails) stepContext.getOptions();

        bookingDetails.setDestination((String) stepContext.getResult());

        if (bookingDetails.getOrigin().isEmpty()) {
            Activity promptMessage =
                MessageFactory
                    .text(originStepMsgText, originStepMsgText, InputHints.EXPECTING_INPUT);
            PromptOptions promptOptions = new PromptOptions();
            promptOptions.setPrompt(promptMessage);
            return stepContext.prompt("TextPrompt", promptOptions);
        }

        return stepContext.next(bookingDetails.getOrigin());
    }


    private CompletableFuture<DialogTurnResult> confirmStep(WaterfallStepContext stepContext) {
        BookingDetails bookingDetails = (BookingDetails) stepContext.getOptions();

        String messageText =
            String.format(
                "Please confirm, I have you traveling to: %s from: %s. Is this correct?",
                bookingDetails.getDestination(), bookingDetails.getOrigin()
            );
        Activity promptMessage = MessageFactory
            .text(messageText, messageText, InputHints.EXPECTING_INPUT);

        PromptOptions promptOptions = new PromptOptions();
        promptOptions.setPrompt(promptMessage);

        return stepContext.prompt("ConfirmPrompt", promptOptions);
    }


    private CompletableFuture<DialogTurnResult> finalStep(WaterfallStepContext stepContext) {
        if ((Boolean) stepContext.getResult()) {
            BookingDetails bookingDetails = (BookingDetails) stepContext.getOptions();
            return stepContext.endDialog(bookingDetails);
        }

        return stepContext.endDialog(null);
    }

}
