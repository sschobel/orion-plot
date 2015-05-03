/**
 * 
 */
package bio.comp.orion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;




import bio.comp.orion.model.MatrixReader;
import bio.comp.orion.model.MatrixReaders;
import bio.comp.orion.presenter.OrionModelPresenter;
import bio.comp.orion.presenter.OrionSVGModelPresenter;

import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.transcoder.image.TIFFTranscoder;
import org.apache.batik.transcoder.svg2svg.SVGTranscoder;
import org.w3c.dom.svg.SVGDocument;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.StandardSystemProperty;
import com.google.common.collect.*;

/**
 * @author roberts
 *
 */
public class OrionCommandLine {

	protected static class ConversionCommand {
		private String inputPath;
		private String outputPath;
		private String inputFormat = "csv";
		private String outputFormat;

		/**
		 * @return the inputPath
		 */
		public String getInputPath() {
			return inputPath;
		}

		public File getInputFile(){
			return inputPath != null ? new File(inputPath) : null;
		}
		
		public File getOutputFile(){
			return outputPath != null ? new File(outputPath) : null;
		}
		/**
		 * @param inputPath
		 *            the inputPath to set
		 */
		public void setInputPath(String inputPath) {
			this.inputPath = inputPath;
		}

		/**
		 * @return the outputPath
		 */
		public String getOutputPath() {
			File outputFile = getOutputFile();
			if(outputFile != null && !outputFile.isAbsolute()){
				File inputFile = getInputFile();
				String sep = StandardSystemProperty.FILE_SEPARATOR.value();
				String path = inputFile.getParent() + 
						sep
                        + outputPath;
				return path;
			}
			else{
				return outputPath;
			}
		}

		/**
		 * @param outputPath
		 *            the outputPath to set
		 */
		public void setOutputPath(String outputPath) {
			this.outputPath = outputPath;
		}

		/**
		 * @return the inputFormat
		 */
		public String getInputFormat() {
			return inputFormat;
		}

		/**
		 * @param inputFormat
		 *            the inputFormat to set
		 */
		public void setInputFormat(String inputFormat) {
			this.inputFormat = inputFormat;
		}

		/**
		 * @return the outputFormat
		 */
		public String getOutputFormat() {
			return outputFormat;
		}

		/**
		 * @param outputFormat
		 *            the outputFormat to set
		 */
		public void setOutputFormat(String outputFormat) {
			this.outputFormat = outputFormat;
		}

		public ConversionCommand() {

		}

	}

	protected static class InputArgument extends Argument {
		public InputArgument(String name) {
			super(name, "Input file to process");
		}
	}

	private static class OutputOption extends Option {
		public OutputOption() {
			super("out", "o", "Path to output file");
			// TODO Auto-generated constructor stub
		}
	}

	private static class FormatOption extends Option {
		public FormatOption() {
			super("format", "f", "Output format");
			addChoice("svg");
			addChoice("png");
			addChoice("tiff");
			addChoice("jpeg");
		}
	}

	
	private static void printHelp(Option[] options){
		System.err.printf("%s - OrionPlot command line tool\n", OrionCommandLine.class.getName());
		System.err.printf("Usage: \n");
		System.err.printf("java -jar <jarfile> %s <options> <input_file>.csv\n",OrionCommandLine.class.getName());
		System.err.printf("Options:\n");
		System.err.printf("\t--help, -h, -? - Print this message\n");
		for(Option opt : options){
			System.err.printf("\t%s\n", opt.toHelpString());
		}
		System.err.printf("Notes:\n");
		System.err.printf("- Multiple output formats are permitted for a single input\n");
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Logger errors = Logger.getLogger("errors");
		Logger debug = Logger.getLogger("debug");
		Option formats = new FormatOption();
		Option outputs = new OutputOption();
		Option[] options = new Option[] { formats, outputs };
		Argument inputArgs = new InputArgument("inputs");
		Map<String, Transcoder> typeTranscoders = new HashMap<String, Transcoder>(){{
			TranscodingHints imghints = new TranscodingHints();
			imghints.put(ImageTranscoder.KEY_BACKGROUND_COLOR, Color.white);
			Transcoder tcode = new PNGTranscoder();
			tcode.setTranscodingHints(imghints);
			put("png" , tcode);
			put("svg", new SVGTranscoder());
			tcode = new TIFFTranscoder();
			tcode.setTranscodingHints(imghints);
			put("tiff", tcode);
			JPEGTranscoder jpegTranscoder = new JPEGTranscoder();
			TranscodingHints jpegHints = new TranscodingHints(imghints);
			jpegHints.put(JPEGTranscoder.KEY_QUALITY, new Float(.9));
			jpegTranscoder.setTranscodingHints(jpegHints);;
			put("jpeg", jpegTranscoder);
		}};
		
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if (arg.equals("-h") || arg.equals("--help") || arg.equals("-?")){
				printHelp(options);
				System.exit(0);
			}
			if (Option.isOption(arg) || Option.isShortOption(arg)) {
				Option matchedOption = null;
				for (Option opt : options) {
					if (opt.matchesArg(arg)) {
						matchedOption = opt;
						break;
					}
				}
				if (matchedOption != null) {
					i++;
					if (i < args.length) {
						String opt = arg;
						arg = args[i];
						if (Option.isOption(arg) || Option.isShortOption(arg)) {
							errors.log(Level.WARNING,
									"encountered option without value %s", opt);
						} else if (!matchedOption.isValidChoice(arg)) {
							errors.fine(String
									.format("value %s is an invalid choice for option %s, valid choices are %s",
											arg, opt,
											matchedOption.validChoices()));
						} else {
							matchedOption.addValue(arg);
						}
					}

				} else {
					errors.log(Level.WARNING, String.format(
							"encountered unknown option %s\n", arg));
				}

			} else {
				debug.log(Level.INFO,
						String.format("encountered input argument %s", arg));
				inputArgs.addValue(arg);

			}

		}
		debug.log(Level.INFO, String.format("Options: \n %s \nInputs: \n %s",
				Joiner.on('\n').join(options), inputArgs));
		List<ConversionCommand> conversions = new ArrayList<ConversionCommand>();
		for (int f = 0; f < inputArgs.values().length; f++) {
			String inputFile = inputArgs.values()[f];
			for (int i = 0; i < formats.values().length; i++) {
				String format = formats.values()[i];
				String output = null;
				if (i < outputs.values().length) {
					output = outputs.values()[i];
				}
				ConversionCommand conv = new ConversionCommand();
				conv.setInputPath(inputFile);
				conv.setOutputPath(output);
				conv.setOutputFormat(format);
				conversions.add(conv);
			}
		}
		debug.log(
				Level.INFO,
				String.format("Conversions %s",
						Joiner.on("\n").join(conversions)));
		for (ConversionCommand conversionCommand : conversions) {
			MatrixReader reader = MatrixReaders
					.readerForFile(conversionCommand.inputPath);
			OrionModelPresenter<SVGDocument> presenter = new OrionSVGModelPresenter(
					reader.getModel());
			SVGDocument doc = OrionSVGModelPresenter.createEmptySVGDocument();
			presenter.present(doc);
			Transcoder trans = typeTranscoders.get(conversionCommand.getOutputFormat());
			if(trans != null){
			try {
				TranscoderOutput tout = null;
				String fmt = conversionCommand.getOutputFormat();
				ByteArrayOutputStream baos = null;
				if(fmt.equalsIgnoreCase("svg")){
					tout = new TranscoderOutput(new FileWriter(
							conversionCommand.getOutputFile()
							));
				}
				else{
					baos = new ByteArrayOutputStream();
					tout = new TranscoderOutput(baos);

				}
				errors.info(String.format("Converting file to %s", conversionCommand.getOutputFormat()));
				trans.transcode(
						new TranscoderInput(doc),
						tout
						);
				if(baos != null){

					OutputStream os = new FileOutputStream(conversionCommand.getOutputPath(), false);
					byte[] bytearr = baos.toByteArray();
					errors.info(String.format("writing %d bytes to %s",  bytearr.length, conversionCommand.getOutputPath()));
					os.write(bytearr, 0, bytearr.length);
					os.flush();
					os.close();
					os = null;
				}
				errors.info(String.format("Conversion complete %s", conversionCommand.getOutputPath()));

			} catch (TranscoderException e) {
				errors.log(
						Level.SEVERE,  
						String.format("Could not convert %s to format '%s'", conversionCommand.getInputPath(), conversionCommand.getOutputFormat()), 
						e
						);
				System.exit(1);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				errors.log(
						Level.SEVERE,  
						String.format("Could not write svg document to '%s'", conversionCommand.getOutputPath()),
						e
						);
				System.exit(2);
			}
			}
			else {
				errors.log(Level.SEVERE, String.format("Conversion to %s is not currently supported.",conversionCommand.getOutputFormat()));
			}
		}
	}
}
