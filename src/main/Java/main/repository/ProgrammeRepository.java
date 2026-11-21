package main.repository;

import java.util.ArrayList;
import java.util.List;
import main.model.Programme;
import main.model.UndergraduateProgramme;
import main.model.PostgraduateProgramme;

public class ProgrammeRepository {

    private final String filePath;

    public ProgrammeRepository(String filePath) {
        this.filePath = filePath;
    }

    public List<Programme> loadAll() {
        List<String[]> rows = CsvUtil.readAll(filePath);
        List<Programme> programmes = new ArrayList<>();

        for (int i = 1; i < rows.size(); i++) {
            String[] r = rows.get(i);
            if (r.length < 4) {
                continue;
            }

            Programme p;
            if (r[3].equals("UNDERGRAD")) {
                p = new UndergraduateProgramme(r[0], r[1], r[2]);
            } else {
                p = new PostgraduateProgramme(r[0], r[1], r[2]);
            }
            programmes.add(p);
        }
        return programmes;
    }

    public Programme findByCode(String code) {
        List<Programme> programmes = loadAll();
        for (Programme p : programmes) {
            if (p.getCode().equals(code)) {
                return p;
            }
        }
        return null; // Return null if no programme with the given code is found
    }

    public void save(Programme p) {
        List<Programme> programmes = loadAll();
        programmes.add(p);
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"code", "title", "department", "type"}); // header
        for (Programme programme : programmes) {
            rows.add(new String[]{programme.getCode(), programme.getTitle(), programme.getDepartment(), programme.getType()});
        }
        CsvUtil.writeAll(filePath, rows);
    }
}