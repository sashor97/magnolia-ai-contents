package org.formentor.magnolia.ai.infrastructure.openai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Given a prompt and an instruction, the model will return an edited version of the prompt..
 * All fields are nullable.
 *
 * https://beta.openai.com/docs/api-reference/edits/create
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class EditRequest {

    /**
     *ID of the model to use. You can use the List models API to see all of your available models, or see our Model overview for descriptions of them.
   */
    String model;

    /**
     The input text to use as a starting point for the edit.
     */
    String input;

    /**
     The instruction that tells the model how to edit the prompt.
     */
    String instruction;


    /**
     How many edits to generate for the input and instruction.
     */
    Integer n;

    /**
     What sampling temperature to use. Higher values means the model will take more risks. Try 0.9 for more creative applications, and 0 (argmax sampling) for ones with a well-defined answer.
     We generally recommend altering this or top_p but not both.
     */
    Double temperature;

    /**
     An alternative to sampling with temperature, called nucleus sampling, where the model considers the results of the tokens with top_p probability mass. So 0.1 means only the tokens comprising the top 10% probability mass are considered.
     We generally recommend altering this or temperature but not both.
     */
    Double top_p;

}
