package util;

import java.io.FileWriter;
import java.io.BufferedWriter;

public class OutputWriter {
	private static String fileName = "";
	private static FileWriter fileWriter = null;
	private static BufferedWriter bufWriter = null;

	/**
	 * Set the writer's target file. If the given file name is already existed,
	 * the writer appends new content to the file
	 * 
	 * @param newName
	 *            a string, which directs to the file to be written (append) to
	 */

	public static void setOutputFile(String newName) {
		try {
			fileName = newName;
			fileWriter = new FileWriter(fileName, true);
			bufWriter = new BufferedWriter(fileWriter);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Simply write a string followed by new line character to the target file
	 * 
	 * @param line
	 *            a string, which contains the content to be written
	 */
	public static void writeLine(String line) {
		try {
			bufWriter.write(line + "\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Simply close the writer
	 */
	public static void close() {
		try {
			bufWriter.flush();
			fileWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Maybe there is no agent");
			System.exit(1);
		} 
	}
}
