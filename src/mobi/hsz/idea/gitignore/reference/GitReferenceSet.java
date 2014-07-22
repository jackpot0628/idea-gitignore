package mobi.hsz.idea.gitignore.reference;

import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceHelper;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet;
import mobi.hsz.idea.gitignore.psi.GitignoreEntry;
import mobi.hsz.idea.gitignore.util.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

public class GitReferenceSet extends FileReferenceSet {
    public GitReferenceSet(@NotNull GitignoreEntry element) {
        super(element);
    }

    @Override
    public FileReference createFileReference(TextRange range, int index, String text) {
        return new GitReference(this, range, index, text);
    }

    @Override
    public boolean isEndingSlashNotAllowed() {
        return false;
    }

    @NotNull
    @Override
    public Collection<PsiFileSystemItem> computeDefaultContexts() {
        PsiFile containingFile = getElement().getContainingFile();
        PsiDirectory containingDirectory = containingFile.getParent();
        return containingDirectory != null ? Collections.<PsiFileSystemItem>singletonList(containingDirectory) : super.computeDefaultContexts();
    }

    private class GitReference extends FileReference {
        public GitReference(@NotNull FileReferenceSet fileReferenceSet, TextRange range, int index, String text) {
            super(fileReferenceSet, range, index, text);
        }

        @Override
        protected void innerResolveInContext(@NotNull String text, @NotNull PsiFileSystemItem context, Collection<ResolveResult> result, boolean caseSensitive) {
            super.innerResolveInContext(text, context, result, caseSensitive);
            PsiManager psiManager = PsiManager.getInstance(getElement().getProject());
            VirtualFile contextVirtualFile = context.getVirtualFile();
            if (contextVirtualFile != null) {
                String regexFromGlob = Utils.createRegexFromGlob(getCanonicalText());
                for (VirtualFile file : contextVirtualFile.getChildren()) {
                    if (file.getName().matches(regexFromGlob)) {
                        PsiFileSystemItem psiFileSystemItem = FileReferenceHelper.getPsiFileSystemItem(psiManager, file);
                        if (psiFileSystemItem != null) {
                            result.add(new PsiElementResolveResult(psiFileSystemItem));
                        }
                    }
                }
            }
        }
    }
}
