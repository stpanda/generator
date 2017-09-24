import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Generator {

    private static final String PAGE_DELIMITER = "~";
    private static String headerDelimiter = "|";
    private static String stringDelimiter = "";
    private static int count = 0;
    public static int width;
    public static int height;
    public static List<String> titles = new ArrayList<>();
    public static List<Integer> widths = new ArrayList<>();


    public static void main(String[] args) {
        settingsLoader(args[0]);
        writeData(args[1], args[2]);
    }

    private static void settingsLoader(String settingsFile){

        try {
            XMLStreamReader settingsReader = XMLInputFactory.newInstance().createXMLStreamReader(settingsFile, new FileInputStream(settingsFile));
            while (settingsReader.hasNext()){
                settingsReader.next();
                if (settingsReader.isStartElement()){
                    if (settingsReader.getLocalName().equals("width")){
                        settingsReader.next();
                        widths.add(Integer.parseInt(settingsReader.getText()));
                    }else if (settingsReader.getLocalName().equals("title")){
                        settingsReader.next();
                        titles.add(settingsReader.getText());
                    } else if (settingsReader.getLocalName().equals("height")){
                        settingsReader.next();
                        height = Integer.parseInt(settingsReader.getText());
                    }
                }
            }
            settingsReader.close();

        }catch (FileNotFoundException | XMLStreamException e){
            e.printStackTrace();
        }
    }

    private static void writeData(String inputFile, String outputFile){

        width = widths.get(0);

        int[] lengthOfParameters = new int[widths.size() - 1];
        for (int i = 0; i < lengthOfParameters.length; i++) {
            lengthOfParameters[i] = widths.get(i + 1);
        }

        for (int i = 0; i < titles.size(); i++) {
            headerDelimiter += " " + lineFilling(titles.get(i), lengthOfParameters[i], ' ') + " |";
        }

        stringDelimiter = lineFilling(stringDelimiter, width, '-');

        BufferedReader reader;
        BufferedWriter bufferedWriter;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "UTF-16"));
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile)));
            bufferedWriter.write(headerDelimiter);
            bufferedWriter.write("\r\n");
            count++;

            while (reader.ready()){
                String line = reader.readLine();

                String[] params = line.split("\\t");
                int maxLines = 0;
                Map<Integer, ArrayList<String>> map = new HashMap<>(params.length);
                for (int i = 0; i < lengthOfParameters.length; i++) {
                    ArrayList<String> separatedParameter = parameterDelimiter(params[i], lengthOfParameters[i]);
                    map.put(i, separatedParameter);
                    if (maxLines < separatedParameter.size()){
                        maxLines = separatedParameter.size();
                    }
                }
                bufferedWriter.write(stringDelimiter);
                bufferedWriter.write("\r\n");
                count++;
                for (int i = 0; i < maxLines; i++) {
                    String resultLine = "|";
                    for (int j = 0; j < lengthOfParameters.length; j++) {
                        if (map.get(j).size() <= i){
                            resultLine += " " + lineFilling("", lengthOfParameters[j], ' ') + " |";
                        }else {
                            resultLine += " " + map.get(j).get(i) + " |";
                        }
                    }
                    printLine(resultLine, bufferedWriter);
                    bufferedWriter.flush();
                }
            }
            reader.close();
            bufferedWriter.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private static String lineFilling(String str, int limit, char ch){
        while (str.length() < limit){
            str += ch;
        }
        return str;
    }

    private static ArrayList<String> parameterDelimiter(String parameter, int limit){
        ArrayList<String> result = new ArrayList<>();
        if (parameter.contains("/")){
            if (parameter.length() > limit){
                result.add(lineFilling(parameter.substring(0, parameter.length() - 4), limit, ' '));
                result.add(lineFilling(parameter.substring(parameter.length() - 4), limit, ' '));
            }
            else {
                result.add(lineFilling(parameter, limit, ' '));
            }
        }else {
            String[] words = parameter.split(" ");
            for (String word : words){
                String balance = word;
                while (balance.length() > limit) {
                    result.add(balance.substring(0, limit));
                    balance = balance.substring(limit);
                }
                if (balance.length() > 0){
                    result.add(lineFilling(balance, limit, ' '));
                }
            }
        }
        return result;
    }

    private static void printLine(String resultLine, BufferedWriter writer) throws IOException {
        if (count == height){
            writer.write(PAGE_DELIMITER);
            writer.write("\r\n");
            count = 0;
        }
        if (count == 0){
            writer.write(headerDelimiter);
            writer.write("\r\n");
            count++;
            writer.write(stringDelimiter);
            writer.write("\r\n");
            count++;
        }
        writer.write(resultLine);
        writer.write("\r\n");
        count++;
    }
}
