/**
* Copyright 2013 IPSoft Inc.
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*   http://www.apache.org/licenses/LICENSE-2.0
*   
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.clearnlp.generation;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.generation.LGAsk;
import com.clearnlp.generation.LGLibEn;
import com.clearnlp.reader.SRLReader;
import com.clearnlp.util.UTInput;

/** @author Jinho D. Choi ({@code jdchoi77@gmail.com}) */
public class LGAskTest
{
	@Test
	public void testGenerateQuestionFromAsk() throws Exception
	{
		SRLReader reader = new SRLReader(0, 1, 2, 3, 4, 5, 6, 7);
		reader.open(UTInput.createBufferedFileReader("src/test/resources/generation/ask.txt"));
		LGAsk ask = new LGAsk();
		DEPTree tree;
		DEPNode root;
		int i;

		String[] questions = {
				"When was the last time that you were able to log into Remedy?",
				"How many users are getting this error message?",
				"Do you need to learn how to read?",
				"Do you want to reset your password?",
				"Do you want to reset your password?",
				"Do you want to reset your password?",
				"Do I want to reset my password?",
				"Should I place an order for you?",
				"Do you want me to place an order for you?",
				"Did you want me to place an order for you?",
				"Is your account locked?",
				"Is your account being locked?",
				"Was your account locked yesterday?",
				"Were you playing basketball?",
				"Are you an existing customer?",
				"Have you registered your account?",
				"Where were you yesterday?",
				"What do you do for a living?",
				"What do you want to buy?",
				"How long have you been waiting for?",
				"How soon do you want the product to be shipped?",
				"What kind of books do you like to buy?",
				"When did your account get locked?",
				"What can I do for you?",
				"What can you do for you?",
				"Who helped you last time?",
				"Which of your accounts is locked?",
				"When should I reset your password?",
				"What is your username?",
				"Please enter your password.",
				"Please enter your password.",
				"Please be patient."};
		
		String[] asks = {
				"Ask when the last time that the user were able to log into Remedy was.",
				"Ask how many users are getting this error message.",
				"Ask whether the user needs to learn how to read.",
				"Ask whether the user wants to reset the user's password.",
				"Ask whether the user wants to reset the user's password.",
				"Ask whether the user wants to reset the user's password.",
				"Ask whether I want to reset my password.",
				"Ask whether I should place an order for the user.",
				"Ask whether the user wants me to place an order for the user.",
				"Ask whether the user wanted me to place an order for the user.",
				"Ask whether the user's account is locked.",
				"Ask whether the user's account is being locked.",
				"Ask whether the user's account was locked yesterday.",
				"Ask whether the user was playing basketball.",
				"Ask whether the user is an existing customer.",
				"Ask whether the user has registered the user's account.",
				"Ask where the user was yesterday.",
				"Ask what the user does for a living.",
				"Ask what the user wants to buy.",
				"Ask how long the user has been waiting for.",
				"Ask how soon the user wants the product to be shipped.",
				"Ask what kind of books the user likes to buy.",
				"Ask when the user's account got locked.",
				"Ask what I can do for the user.",
				"Ask what the user can do for the user.",
				"Ask who helped the user last time.",
				"Ask which of the user's accounts is locked.",
				"Ask when I should reset the user's password.",
				"Ask what the user's username is."};
		
		for (i=0; (tree = reader.next()) != null; i++)
		{
			tree = ask.generateQuestionFromAsk(tree);
			assertEquals(questions[i], LGLibEn.getForms(tree, false, " "));
			
			if (!tree.get(1).isLemma("please"))
			{
				tree = ask.generateAskFromQuestion(tree);
				assertEquals(asks[i], LGLibEn.getForms(tree, false, " "));
				
				root = tree.getFirstRoot().getDependents().get(0).getNode();
				tree = ask.generateQuestionFromDeclarative(root, false);
				assertEquals(questions[i], LGLibEn.getForms(tree, false, " "));				
			}
		}
	}
	
	@Test
	public void testGenerateQuestionFromDeclarative()
	{
		SRLReader reader = new SRLReader(0, 1, 2, 3, 4, 5, 6, 7);
		reader.open(UTInput.createBufferedFileReader("src/test/resources/generation/ask3.txt"));
		LGAsk ask = new LGAsk();
		DEPTree tree;
		int i;

		String[] questions = {
				"Is the cat's name Doug?",
				"Are you a Ninja?",
				"Do you want to reset your password?",
				"Do you want to reset your password?",
				"Do you want to reset your password?",
				"Do I want to reset my password?",
				"Should I place an order for you?",
				"Did you want me to place an order for you?",
				"Is your account locked?",
				"Is your account being locked?",
				"Was your account locked yesterday?",
				"Are you an existing customer?",
				"Have you registered your account?",
				"Where were you yesterday?",
				"What do you do for a living?",
				"What do you want to buy?",
				"How long have you been waiting for?",
				"How soon do you want the product to be shipped?",
				"What kind of books do you like to buy?",
				"When did your account get locked?",
				"What can I do for you?",
				"Who helped you last time?",
				"Which of your accounts is locked?"};
		
		for (i=0; (tree = reader.next()) != null; i++)
			assertEquals(questions[i], LGLibEn.getForms(ask.generateQuestionFromDeclarative(tree, i==1),false," "));
	}
}
