#if USE_OPTUS_DRM
package com.msap.store.drm.android.data;

/**
 * This class contains information for prompting a user to make a choice.
 * @author Edison Chan
 */
public class PromptAction implements Action {
	public static final String TYPE_NAME = "prompt-action";
	private String title;
	private String summary;
	private String description;
	private String[] choiceLabels;
	private Action[] choiceActions;
	
	/**
	 * Get the title for the prompt.
	 * @return prompt title.
	 */
	public String getTitle() {
		return this.title;
	}

	/**
	 * Get the basic summary that should be displayed to the user.
	 * @return prompt summary.
	 */
	public String getSummary() {
		return this.summary;
	}

	/**
	 * Get the full description for the prompt.
	 * @return full description of the prompt.
	 */
	public String getDescription() {
		return this.description;
	}

	/**
   * Get the number of choices available in this prompt. Each choice in a
   * prompt is given an index between 0 and number of choices minus 1.
   * @return number of choice in this prompt.
   */
	public int getChoiceCount() {
		return this.choiceActions.length;
	}

	/**
	 * Get the label of the given choice in this prompt.
   * @return label of the given choice.
   */
	public String getChoiceLabel(int index) {
		return this.choiceLabels[index];
	}

	/**
	 * Get the action to be executed when a user choose the given option..
   * @return action for the choice.
   */
	public Action getChoiceAction(int index) {
		return this.choiceActions[index];
	}

	/**
	 * Get the label of the given choice in this prompt.
   * @return label of the given choice.
   */
	public String[] getChoiceLabels() {
		return this.choiceLabels;
	}

	/**
	 * Get the action to be executed when a user choose the given option..
   * @return action for the choice.
   */
	public Action[] getChoiceActions() {
		return this.choiceActions;
	}

	/**
	 * Construct a new PromptAction object.
	 * @param title title of the prompt.
	 * @param summary summary of the prompt.
	 * @param description full description of the prompt.
	 * @param labels labels for all choices in the prompt.
	 * @param actions actions for all choices in the prompt.
	 */
	PromptAction(String title, String summary, String description, String[] labels, Action[] actions) {
		this.title = title;
		this.summary = summary;
		this.description = description;
		this.choiceLabels = labels;
		this.choiceActions = actions;
	}

	/**
	 * Construct a new PromptAction object.
	 * @param title title of the prompt.
	 * @param summary summary of the prompt.
	 * @param labels labels for all choices in the prompt.
	 * @param actions actions for all choices in the prompt.
	 */
	PromptAction(String title, String summary, String[] labels, Action[] actions) {
		this(title, summary, summary, labels, actions);
	}
};

#endif	//USE_OPTUS_DRM
