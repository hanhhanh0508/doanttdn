package com.sapota.seo.ui;

import com.sapota.seo.service.GeminiPostGeneratorService;
import com.sapota.seo.util.ApiKeyStore;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;

/**
 * Tab "Tạo bài Post (AI)" — hỗ trợ công việc đăng bài LinkedIn (mục 3.1.1
 * trong báo cáo): nhập link bài viết nguồn + Gemini API Key, ứng dụng tự
 * đọc nội dung trang và nhờ Gemini soạn thành 1 bài đăng LinkedIn hoàn chỉnh.
 */
public class PostGeneratorPanel extends JPanel {

    private JTextField txtUrl;
    private JPasswordField txtApiKey;
    private JCheckBox chkSaveKey;
    private JTextField txtExtraInstruction;
    private JButton btnGenerate;
    private JButton btnCopy;
    private JTextArea txtResult;
    private JLabel lblStatus;

    public PostGeneratorPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(buildInputArea(), BorderLayout.NORTH);
        add(buildResultArea(), BorderLayout.CENTER);
    }

    private JPanel buildInputArea() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Tạo nội dung bài Post từ 1 link bài viết (Gemini AI)"));

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row1.add(new JLabel("Link bài viết:"));
        txtUrl = new JTextField(45);
        row1.add(txtUrl);
        panel.add(row1);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row2.add(new JLabel("Gemini API Key:"));
        txtApiKey = new JPasswordField(30);
        txtApiKey.setText(ApiKeyStore.load());
        row2.add(txtApiKey);
        chkSaveKey = new JCheckBox("Lưu API key trên máy này", !ApiKeyStore.load().isBlank());
        row2.add(chkSaveKey);
        JLabel apiHint = new JLabel("(lấy miễn phí tại aistudio.google.com/apikey)");
        apiHint.setFont(apiHint.getFont().deriveFont(Font.ITALIC, 11f));
        apiHint.setForeground(Color.GRAY);
        row2.add(apiHint);
        panel.add(row2);

        JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row3.add(new JLabel("Yêu cầu thêm (không bắt buộc):"));
        txtExtraInstruction = new JTextField(35);
        txtExtraInstruction.setToolTipText("Ví dụ: nhấn mạnh về AI Agent, viết ngắn gọn hơn...");
        row3.add(txtExtraInstruction);
        panel.add(row3);

        JPanel row4 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        btnGenerate = new JButton("Tạo nội dung bài Post");
        btnGenerate.addActionListener(e -> runGenerate());
        row4.add(btnGenerate);
        lblStatus = new JLabel(" ");
        row4.add(lblStatus);
        panel.add(row4);

        return panel;
    }

    private JPanel buildResultArea() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(new JLabel("Nội dung bài Post (có thể chỉnh sửa lại trước khi đăng):"), BorderLayout.NORTH);

        txtResult = new JTextArea(15, 60);
        txtResult.setLineWrap(true);
        txtResult.setWrapStyleWord(true);
        panel.add(new JScrollPane(txtResult), BorderLayout.CENTER);

        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnCopy = new JButton("Copy vào Clipboard");
        btnCopy.addActionListener(e -> copyToClipboard());
        bottomRow.add(btnCopy);
        panel.add(bottomRow, BorderLayout.SOUTH);

        return panel;
    }

    private void runGenerate() {
        String url = txtUrl.getText().trim();
        String apiKey = new String(txtApiKey.getPassword()).trim();
        String extra = txtExtraInstruction.getText().trim();

        if (url.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập link bài viết.",
                    "Thiếu dữ liệu", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (apiKey.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Gemini API Key.",
                    "Thiếu dữ liệu", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (chkSaveKey.isSelected()) {
            ApiKeyStore.save(apiKey);
        } else {
            ApiKeyStore.save("");
        }

        btnGenerate.setEnabled(false);
        lblStatus.setText("Đang đọc bài viết và tạo nội dung...");
        txtResult.setText("");

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                return new GeminiPostGeneratorService().generatePost(url, apiKey, extra);
            }

            @Override
            protected void done() {
                btnGenerate.setEnabled(true);
                try {
                    String result = get();
                    txtResult.setText(result);
                    lblStatus.setText("Đã tạo xong.");
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    lblStatus.setText("Lỗi.");
                    JOptionPane.showMessageDialog(PostGeneratorPanel.this,
                            "Không tạo được nội dung:\n" + cause.getMessage(),
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void copyToClipboard() {
        String text = txtResult.getText();
        if (text.isBlank()) {
            JOptionPane.showMessageDialog(this, "Chưa có nội dung để copy.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
        lblStatus.setText("Đã copy vào clipboard.");
    }
}
