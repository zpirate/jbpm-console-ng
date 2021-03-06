package org.jbpm.console.ng.bd.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jbpm.console.ng.bd.api.FileException;
import org.jbpm.console.ng.bd.api.FileService;
import org.jbpm.kie.services.impl.form.provider.FreemakerFormProvider;
import org.jbpm.kie.services.impl.model.ProcessDesc;
import org.kie.api.task.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uberfire.java.nio.file.Path;

@ApplicationScoped
public class VFSFormProvider extends FreemakerFormProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(VFSFormProvider.class);

    @Inject
    private FileService fileService;

    @Override
    public String render(String name, ProcessDesc process, Map<String, Object> renderContext) {
        if (process == null || process.getOriginalPath() == null) {
            return null;
        }

        InputStream template = null;
        Iterable<Path> availableForms = null;
        Path processPath = fileService.getPath(process.getOriginalPath());
        Path formsPath = fileService.getPath(processPath.getParent().toUri().toString() + "/forms/");
        try {

            if(fileService.exists(formsPath)){
                availableForms = fileService.loadFilesByType(formsPath, "ftl");
            }
        } catch (FileException ex) {
            logger.error("File exception", ex);
        }
        Path selectedForm = null;
        if(availableForms != null){
            for (Path p : availableForms) {
                if (p.getFileName().toString().contains(process.getId())) {
                    selectedForm = p;
                }
            }
        }

        try {
            if (selectedForm == null) {
                String rootPath = processPath.getRoot().toUri().toString();
                if (!rootPath.endsWith(processPath.getFileSystem().getSeparator())) {
                    rootPath +=processPath.getFileSystem().getSeparator();
                }

                Path defaultFormPath = fileService.getPath(rootPath +"globals/forms/DefaultProcess.ftl");
                if (fileService.exists(defaultFormPath)) {
                    template = new ByteArrayInputStream(fileService.loadFile(defaultFormPath));
                }

            } else {

                template = new ByteArrayInputStream(fileService.loadFile(selectedForm));

            }
        } catch (FileException ex) {
            logger.error("File exception", ex);
        }

        if (template == null) return null;

        return render(name, template, renderContext);
    }

    @Override
    public String render(String name, Task task, ProcessDesc process, Map<String, Object> renderContext) {
        InputStream template = null;
        Path processPath = null;

        Iterable<Path> availableForms = null;
        try {
            if(process != null && process.getOriginalPath() != null){
                processPath = fileService.getPath(process.getOriginalPath());
                Path formsPath = fileService.getPath(processPath.getParent().toUri().toString() + "/forms/");
                if(fileService.exists(formsPath)){
                    availableForms = fileService.loadFilesByType(formsPath, "ftl");
                }
            }
        } catch (FileException ex) {
            logger.error("File exception", ex);
        }
        Path selectedForm = null;
        if(availableForms != null){
            for (Path p : availableForms) {
                if (p.getFileName().toString().contains(task.getNames().get(0).getText())) {
                    selectedForm = p;
                }
            }
        }

        try {
            if (selectedForm == null) {
                String rootPath = "";
                if(processPath != null){
                    rootPath = processPath.getRoot().toUri().toString();
                    if (!rootPath.endsWith(processPath.getFileSystem().getSeparator())) {
                        rootPath +=processPath.getFileSystem().getSeparator();
                    }
                }
                if(!rootPath.equals("")){
                    Path defaultFormPath = fileService.getPath(rootPath +"globals/forms/DefaultTask.ftl");
                    if (fileService.exists(defaultFormPath)) {
                        template = new ByteArrayInputStream(fileService.loadFile(defaultFormPath));
                    }
                }
            } else {
                template = new ByteArrayInputStream(fileService.loadFile(selectedForm));
            }
        } catch (FileException ex) {
            logger.error("File exception", ex);
        }

        if (template == null) return null;

        return render(name, template, renderContext);
    }

    @Override
    public int getPriority() {
        return 1;
    }

}