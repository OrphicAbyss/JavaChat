package javachat;

/**
 * Implements a println method to output text to the user.
 * 
 * @author DrLabman
 */
public interface PrintOutput {
	public void print(Object source, final String text);
	public void println(Object source, final String text);
}
