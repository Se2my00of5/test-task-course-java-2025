import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class Configuration {

    public enum StatMode {
        NONE,  // Статистика не требуется
        SHORT, // Краткая статистика (-s)
        FULL   // Полная статистика (-f)
    }

    private Path outputPath = Paths.get("."); // По умолчанию - текущая директория
    private String prefix = ""; // По умолчанию - без префикса
    private boolean appendMode = false; // По умолчанию - режим перезаписи
    private StatMode statMode = StatMode.NONE; // По умолчанию - статистика отключена
    private final Set<String> inputFiles = new LinkedHashSet<>();

    public boolean addInputFile(String file) {
        return inputFiles.add(file);
    }
}