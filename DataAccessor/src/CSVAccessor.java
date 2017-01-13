import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class CSVAccessor implements IDataAccessor {

    private final String LINE_SEPARATOR = ",";
    private String filePath;

    public CSVAccessor(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public List<DBEntry> readDatabase() {

        ArrayList<DBEntry> entries = null;

        try {
            BufferedReader br = new BufferedReader(new FileReader(this.filePath));

            String line = "";

            entries = new ArrayList<>();

            int counter = 1;
            while ((line = br.readLine()) != null){
                String[] fields = line.split(LINE_SEPARATOR);

                if(fields.length != 3)
                {
                    System.out.println("Line " + counter + " corrupted, " + (3 - fields.length) + " entries missing.");
                }else {

                    DBEntry entry = new DBEntry(fields[0].trim(), fields[2].trim(), fields[1].trim(), null);

                    entries.add(entry);
                }
                counter++;
            }

            System.out.println("Processed " + counter + " lines, finished.");

        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return entries;
    }
}
