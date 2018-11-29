package introdb.heap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

class SimpleEntryTest {

    private Path filePath;

    @BeforeEach
    public void setUp() throws IOException {
        filePath = Files.createTempFile("heap", "0001");
    }

    @AfterEach
    public void tearDown() throws IOException {
        Files.delete(filePath);
    }

    @Test
    void save_two_times_read_two_times() throws IOException, ClassNotFoundException {
        save(new SimpleEntry(2, 5));
        save(new SimpleEntry(8, 23));

        try (FileInputStream fis = new FileInputStream(filePath.toFile())) {
            try (ObjectInputStream input = new ObjectInputStream(fis)) {
                boolean cont = true;
                while (cont) {
                    SimpleEntry page = (SimpleEntry) input.readObject();
                    if (page != null) {
                        System.out.println(page);
                    } else {
                        cont = false;
                    }
                }
            }
        }
    }

    private void save(SimpleEntry simpleEntry) throws IOException {
        try (FileOutputStream fout = new FileOutputStream(filePath.toFile(), true)) {
            try (ObjectOutputStream oos = new ObjectOutputStream(fout)) {
                oos.writeObject(simpleEntry);
            }
        }
    }
}
