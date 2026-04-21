package net.mokatech.ij.plugin.annotate.listeners;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.AsyncFileListener;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileMoveEvent;
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent;
import net.mokatech.ij.plugin.annotate.model.AnnotationInfos;
import net.mokatech.ij.plugin.annotate.services.AnnotationService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FileTrackerListener implements AsyncFileListener {

    private final Project project;

    public FileTrackerListener(Project project) {
        this.project = project;
    }

    @Override
    @Nullable
    public AsyncFileListener.ChangeApplier prepareChange(@NotNull List<? extends VFileEvent> events) {
        List<String[]> pathChanges = new ArrayList<>();
        List<String> deletes = new ArrayList<>();
        for (VFileEvent event : events) {
            switch (event) {
                // File renamed
                case VFilePropertyChangeEvent propEvent -> {
                    if (!VirtualFile.PROP_NAME.equals(propEvent.getPropertyName())) continue;
                    VirtualFile file = propEvent.getFile();
                    if (!file.isValid() || file.isDirectory()) continue;

                    String oldName = (String) propEvent.getOldValue();
                    String newName = (String) propEvent.getNewValue();
                    if (oldName == null || newName == null || oldName.equals(newName)) continue;

                    String parentPath = file.getParent().getPath();
                    String oldPath = parentPath + "/" + oldName;
                    String newPath = parentPath + "/" + newName;
                    pathChanges.add(new String[]{oldPath, newPath});
                }
                // File moved
                case VFileMoveEvent moveEvent -> {
                    VirtualFile file = moveEvent.getFile();
                    if (!file.isValid() || file.isDirectory()) continue;

                    String fileName = moveEvent.getFile().getName();
                    String oldPath = moveEvent.getOldParent().getPath() + "/" + fileName;
                    String newPath = moveEvent.getNewParent().getPath() + "/" + fileName;
                    pathChanges.add(new String[]{oldPath, newPath});
                }
                // File deleted
                case VFileDeleteEvent deleteEvent -> {
                    VirtualFile file = deleteEvent.getFile();
                    if (!file.isValid() || file.isDirectory()) continue;

                    String filePath = file.getPath();
                    deletes.add(filePath);
                }
                case null, default -> {
                }
            }
        }
        if (pathChanges.isEmpty() && deletes.isEmpty()) {
            return null;
        }
        return new ChangeApplierImpl(project, pathChanges, deletes);
    }

    private static class ChangeApplierImpl implements AsyncFileListener.ChangeApplier {
        private final Project project;
        private final List<String[]> pathChanges;
        private final List<String> deletes;

        ChangeApplierImpl(Project project, List<String[]> pathChanges, List<String> deletes) {
            this.project = project;
            this.pathChanges = pathChanges;
            this.deletes = deletes;
        }

        @Override
        public void afterVfsChange() {
            AnnotationService service = AnnotationService.getInstance(project);
            for (String[] change : pathChanges) {
                String oldPath = change[0];
                String newPath = change[1];
                List<AnnotationInfos> anns = service.findForFile(oldPath);
                for (AnnotationInfos ann : anns) {
                    ann.filePath = newPath;
                }
            }
            for (String filePath : deletes) {
                service.removeForFile(filePath);
            }
        }
    }
}