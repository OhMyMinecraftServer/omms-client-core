package net.zhuruoling.omms.client.system;

import net.zhuruoling.omms.client.util.Result;

import java.util.List;

public class DirectoryInfo {
    List<String> folders = null;
    List<String> files = null;

    private Result result = Result.__NULL;

    public DirectoryInfo() {
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public DirectoryInfo(List<String> folders, List<String> files) {
        this.folders = folders;
        this.files = files;
    }

    public List<String> getFolders() {
        return folders;
    }

    public void setFolders(List<String> folders) {
        this.folders = folders;
    }

    public List<String> getFiles() {
        return files;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }

}
