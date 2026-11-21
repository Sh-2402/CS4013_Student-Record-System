package main.repository;

import java.util.ArrayList;
import java.util.List;
import main.model.Module;

public class ModuleRepository {

    private final String filePath;

    public ModuleRepository(String filePath) {
        this.filePath = filePath;
    }

    public List<Module> loadAll() {
        List<String[]> rows = CsvUtil.readAll(filePath);
        List<Module> modules = new ArrayList<>();

        for (int i = 1; i < rows.size(); i++) {      // start at 1 to skip the header
            String[] r = rows.get(i);
            if (r.length < 5) {                        // skip blank or broken lines
                continue;
            }
            Module m = new Module(r[0], r[1], Integer.parseInt(r[2]), Integer.parseInt(r[3]), r[4]);
            modules.add(m);
        }
        return modules;
    }

    public Module findByCode(String code){
        List<Module> modules = loadAll();
        for (Module m : modules) {
            if (m.getCode().equals(code)) {
                return m;
            }
        }
        return null; // Return null if no module with the given code is found
        }

     public void save(Module m) {
        List<Module> modules = loadAll();
        modules.add(m);
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"code", "title", "credits", "semester", "lecturerId"}); // header
        for (Module module : modules) {
            rows.add(new String[]{module.getCode(), module.getTitle(), String.valueOf(module.getCredits()),
                    String.valueOf(module.getSemester()), module.getLecturerId()});
        }
        CsvUtil.writeAll(filePath, rows);
     }   
    }
