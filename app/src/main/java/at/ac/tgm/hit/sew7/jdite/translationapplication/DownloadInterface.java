package at.ac.tgm.hit.sew7.jdite.translationapplication;

import android.os.Binder;

import java.util.List;

public interface DownloadInterface {
    public void addLanguages(List<Language> languages);
    public void downloadedLanguage(Language language);
    public void notifyDownload(int numberOfLangs);
}
