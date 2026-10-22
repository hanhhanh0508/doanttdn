package com.sapota.seo.ui;

/** Panel nào cần nạp lại dữ liệu khi người dùng chuyển sang tab của nó thì implement interface này. */
public interface Refreshable {
    void refresh();
}
