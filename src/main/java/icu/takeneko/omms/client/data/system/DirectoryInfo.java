package icu.takeneko.omms.client.data.system;

import icu.takeneko.omms.client.util.Result;
import lombok.Getter;

import java.util.List;

@Getter
public class DirectoryInfo {
    List<String> folders = null;
    List<String> files = null;

    private final Result result = Result.UNDEFINED;

    public DirectoryInfo() {
    }

    public DirectoryInfo(List<String> folders, List<String> files) {
        this.folders = folders;
        this.files = files;
    }

}
