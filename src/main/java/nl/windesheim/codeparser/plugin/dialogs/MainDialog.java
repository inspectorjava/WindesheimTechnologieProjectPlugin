package nl.windesheim.codeparser.plugin.dialogs;

import com.github.javaparser.Range;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import nl.windesheim.codeparser.plugin.action_listeners.MainDialogActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.tree.TreePath;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * The MianDialog from where all the action happens.
 */
public class MainDialog {

    /**
     * The wrapper panel for all the other elements.
     */
    private JPanel toolPanel;

    /**
     * Button to refresh all the found design patterns.
     */
    private JButton refreshButton;

    /**
     * Tree list that should show all the found design patterns.
     */
    private JTree patternsList;

    /**
     * Message to provide the user with a last updated at timestamp.
     */
    private JLabel lastUpdateText;

    /**
     * Constructor for this class, used to add an action listener.
     */
    public MainDialog() {
        this.patternsList.setEditable(false);
        this.patternsList.setCellRenderer(new PatternTreeCellRenderer());
        this.refreshButton.addActionListener(
                new MainDialogActionListener(this.lastUpdateText, this.patternsList)
        );

        ToolTipManager.sharedInstance().registerComponent(this.patternsList);
        patternsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(final MouseEvent event) {
                int selRow = getPatternsList().getRowForLocation(event.getX(), event.getY());
                if (selRow == -1) {
                    return;
                }

                if (event.getClickCount() != 2) {
                    return;
                }

                TreePath selPath = getPatternsList().getPathForLocation(event.getX(), event.getY());
                if (!(selPath.getLastPathComponent() instanceof PatternTreeNode)) {
                    return;
                }

                PatternTreeNode node = (PatternTreeNode) selPath.getLastPathComponent();
                if (node.getFilePart() == null) {
                    return;
                }

                //Get the current project depending on the focus
                DataContext dataContext = DataManager.getInstance().getDataContextFromFocus().getResult();
                if (dataContext == null) {
                    return;
                }

                Project project = DataKeys.PROJECT.getData(dataContext);

                if (project == null) {
                    return;
                }

                VirtualFile vFile = LocalFileSystem.getInstance()
                        .findFileByIoFile(node.getFilePart().getFile());

                if (vFile != null) {
                    Range range = node.getFilePart().getRange();
                    new OpenFileDescriptor(project, vFile, range.begin.line, range.begin.column).navigate(true);
                }
            }
        });
    }

    /**
     * @return the tool panel
     */
    public JPanel getToolPanel() {
        return toolPanel;
    }

    /**
     * @return the pattern list
     */
    public JTree getPatternsList() {
        return patternsList;
    }
}
