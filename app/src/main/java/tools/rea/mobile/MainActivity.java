package tools.rea.mobile;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.database.Cursor;
import android.provider.OpenableColumns;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Locale;

public class MainActivity extends Activity {
    LinearLayout root, content;
    TextView status;
    static final int PICK_FILE = 401;

    int bg = 0xFF0B1020, panel = 0xFF151D31, text = 0xFFF0F4FF, muted = 0xFFA8B4CC, accent = 0xFF76E4C3;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(bg);
        getWindow().setNavigationBarColor(bg);
        showHome();
    }

    int dp(float v) { return (int)(v * getResources().getDisplayMetrics().density + .5f); }
    TextView label(String s, int size, int color) {
        TextView t = new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color);
        t.setPadding(dp(2), dp(3), dp(2), dp(3)); return t;
    }
    void setup(String title, String subtitle) {
        root = new LinearLayout(this); root.setOrientation(1); root.setBackgroundColor(bg);
        root.setPadding(dp(18), dp(22), dp(18), dp(14));
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true);
        content = new LinearLayout(this); content.setOrientation(1);
        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        TextView foot = label("REA MOBILE  •  LOCAL-FIRST TOOLBOX", 11, muted);
        foot.setGravity(Gravity.CENTER); root.addView(foot);
        setContentView(root);
        TextView logo = label("REA", 32, accent); logo.setTypeface(null, Typeface.BOLD); content.addView(logo);
        TextView h = label(title, 23, text); h.setTypeface(null, Typeface.BOLD); content.addView(h);
        TextView sub = label(subtitle, 14, muted); sub.setPadding(2, 0, 2, dp(14)); content.addView(sub);
    }
    void card(String title, String desc, String button, Runnable action) {
        LinearLayout box = new LinearLayout(this); box.setOrientation(1); box.setPadding(dp(14), dp(12), dp(14), dp(12));
        box.setBackgroundColor(panel);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2); lp.bottomMargin = dp(10); content.addView(box, lp);
        TextView t = label(title, 17, text); t.setTypeface(null, Typeface.BOLD); box.addView(t);
        TextView d = label(desc, 13, muted); d.setPadding(2, dp(4), 2, dp(8)); box.addView(d);
        Button b = new Button(this); b.setText(button); b.setTextColor(bg); b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(accent));
        b.setOnClickListener(v -> action.run()); box.addView(b);
    }
    void showHome() {
        setup("Reverse-engineering workspace", "A lightweight Android companion inspired by the REA project.");
        card("Inspect a file", "Choose an APK, DEX, ELF/shared library, ZIP or any other file. Compute SHA-256, size and a printable-string preview locally.", "CHOOSE FILE", this::pickFile);
        card("REA capabilities", "The original REA is a Node.js CLI/MCP server for agent-assisted reverse engineering. It connects desktop analysis tools such as Ghidra, Hopper, IDA and JADX.", "READ OVERVIEW", this::showOverview);
        card("Quick-start commands", "View the original project's setup commands and prerequisites.", "VIEW COMMANDS", this::showCommands);
        TextView note = label("Note: this Android app is a companion utility, not the complete REA CLI/MCP server. Desktop integrations require compatible host tools and Node.js.", 12, muted);
        note.setPadding(2, dp(5), 2, dp(12)); content.addView(note);
    }
    void pickFile() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT); i.setType("*/*"); i.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(i, PICK_FILE);
    }
    @Override protected void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req == PICK_FILE && res == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                String name = getName(uri); MessageDigest md = MessageDigest.getInstance("SHA-256");
                long size = 0; StringBuilder strings = new StringBuilder(); StringBuilder preview = new StringBuilder();
                try (InputStream in = getContentResolver().openInputStream(uri)) {
                    byte[] buf = new byte[8192]; int n; StringBuilder run = new StringBuilder();
                    while ((n = in.read(buf)) != -1) {
                        md.update(buf, 0, n); size += n;
                        for (int x=0; x<n; x++) {
                            int c = buf[x] & 255;
                            if (c >= 32 && c <= 126) { if (run.length() < 160) run.append((char)c); }
                            else { if (run.length() >= 5 && preview.length() < 4500) preview.append(run).append('\n'); run.setLength(0); }
                        }
                    }
                    if (run.length() >= 5 && preview.length() < 4500) preview.append(run).append('\n');
                }
                StringBuilder hash = new StringBuilder(); for (byte b : md.digest()) hash.append(String.format(Locale.US, "%02x", b));
                showAnalysis(name, size, hash.toString(), preview.toString());
            } catch (Exception e) { showMessage("Could not inspect file", e.toString()); }
        }
    }
    String getName(Uri uri) {
        String name = uri.getLastPathSegment();
        try (Cursor c = getContentResolver().query(uri, null, null, null, null)) {
            if (c != null && c.moveToFirst()) { int ix = c.getColumnIndex(OpenableColumns.DISPLAY_NAME); if (ix >= 0) name = c.getString(ix); }
        } catch (Exception ignored) {}
        return name == null ? "Selected file" : name;
    }
    void showAnalysis(String name, long size, String hash, String strings) {
        setup("File inspection", "Computed on-device. File contents are not uploaded.");
        card("File", name, "BACK", this::showHome);
        addInfo("Size", String.format(Locale.US, "%,d bytes (%.2f MiB)", size, size / 1048576.0));
        addInfo("SHA-256", hash);
        TextView st = label("PRINTABLE STRINGS PREVIEW", 12, accent); st.setTypeface(null, Typeface.BOLD); content.addView(st);
        TextView body = label(strings.isEmpty() ? "No printable strings of 5+ characters found." : strings, 12, text);
        body.setTextIsSelectable(true); body.setPadding(dp(10), dp(10), dp(10), dp(10)); body.setBackgroundColor(panel); content.addView(body);
        TextView warning = label("Strings are clues, not proof of program behavior. Use a dedicated disassembler/decompiler for deeper analysis.", 12, muted); content.addView(warning);
    }
    void addInfo(String title, String value) {
        TextView t = label(title, 12, accent); content.addView(t);
        TextView v = label(value, 13, text); v.setTextIsSelectable(true); v.setPadding(dp(10), dp(8), dp(10), dp(8)); v.setBackgroundColor(panel); content.addView(v);
    }
    void showOverview() {
        setup("What REA does", "The upstream project: morluto/rea (rea-agents).");
        addInfo("Purpose", "One CLI/MCP server for reverse engineering applications, binaries, JavaScript/Electron apps and runtime behavior.");
        addInfo("Workflow", "Connect an AI coding agent to REA's MCP tools; ask it to investigate an app feature and cite evidence.");
        addInfo("Analysis engines", "Depending on target and OS, REA can integrate Ghidra, IDA, Hopper, JADX and other tools. These are not bundled in this Android app.");
        addInfo("Android companion", "This app provides local file metadata and SHA-256 plus a printable-strings preview. It does not claim to replace the full upstream desktop toolchain.");
        Button b = new Button(this); b.setText("BACK"); b.setOnClickListener(v -> showHome()); content.addView(b);
    }
    void showCommands() {
        setup("Original REA quick start", "Run these on a supported desktop/terminal, not inside this Android companion.");
        addInfo("Prerequisite", "Node.js 22.19+ or 24.11+ as stated by the upstream package.");
        addInfo("Install/setup", "npx rea-agents setup");
        addInfo("Terminal CLI", "npx rea-agents --help");
        addInfo("Official project", "https://github.com/morluto/rea");
        addInfo("Important", "Native analysis may require separately installed tools and OS-specific bridges. Android does not provide the desktop tool integrations automatically.");
        Button b = new Button(this); b.setText("BACK"); b.setOnClickListener(v -> showHome()); content.addView(b);
    }
    void showMessage(String title, String message) {
        setup(title, ""); addInfo("Details", message);
        Button b = new Button(this); b.setText("BACK"); b.setOnClickListener(v -> showHome()); content.addView(b);
    }
}
