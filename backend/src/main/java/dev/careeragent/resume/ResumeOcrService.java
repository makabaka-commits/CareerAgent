package dev.careeragent.resume;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

@Service
public class ResumeOcrService {
    private final boolean enabled;private final String executable;private final String languages;
    public ResumeOcrService(@Value("${app.ocr.enabled:false}")boolean enabled,@Value("${app.ocr.executable:}")String executable,@Value("${app.ocr.languages:chi_sim+eng}")String languages){this.enabled=enabled;this.executable=executable;this.languages=languages;}
    public String extract(byte[] pdf){
        if(!enabled||executable.isBlank()||!Files.isRegularFile(Path.of(executable)))return "";
        Path temp=null;try{temp=Files.createTempDirectory("stepwise-ocr-");StringBuilder text=new StringBuilder();
            try(var document= Loader.loadPDF(pdf)){PDFRenderer renderer=new PDFRenderer(document);for(int i=0;i<Math.min(document.getNumberOfPages(),6);i++){Path image=temp.resolve("page-"+i+".png");ImageIO.write(renderer.renderImageWithDPI(i,180, ImageType.GRAY),"png",image.toFile());Process process=new ProcessBuilder(executable,image.toString(),"stdout","-l",languages).redirectErrorStream(true).start();String value=new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);if(process.waitFor()==0)text.append(value).append('\n');}}
            return text.toString();
        }catch(Exception ignored){return "";}finally{if(temp!=null)try(var files=Files.walk(temp)){files.sorted(java.util.Comparator.reverseOrder()).forEach(p->{try{Files.deleteIfExists(p);}catch(Exception ignored){}});}catch(Exception ignored){}}
    }
}
