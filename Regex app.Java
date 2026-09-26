package com.example.smaliregex;

import android.app.Activity;
import android.os.Bundle;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import android.util.Base64;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends Activity {

    private LinearLayout root;
    private int currentScreen = 0;

    private final int BLUE = Color.rgb(0, 123, 255);
    private final int DARK = Color.rgb(25, 30, 40);
    private final int GRAY = Color.rgb(100, 108, 120);
    private final int LIGHT = Color.rgb(248, 249, 250);
    private final int BORDER = Color.rgb(222, 226, 230);
    private final int WHITE = Color.WHITE;

    // ============================================================
    // PRE-BUILT PATCHES
    //
    // ADD YOUR PATCHES HERE
    //
    // Format:
    //
    // {"Patch name", "SEARCH TEXT", "REPLACE TEXT"},
    //
    // Add another line underneath for Patch 2, Patch 3, etc.
    // ============================================================

private static final String[][] PATCHES = {

        {
            "Screenshots Restriction Removal 1",

            "(const/)16(\\s([pv]\\d+),\\s0x)200(0(?:(?!\\.end\\smethod)[\\s\\S])*?invoke-virtual\\s\\{[pv]\\d+,\\s\\3(?:,\\s[pv]\\d+)?\\},\\sLandroid/view/Window;->(?:add|set)Flags\\(II?\\)V)",

            "$14$2$4"
        },

        {
            "Screenshots Restriction Removal 2",

            "(invoke-virtual\\s\\{[pv]\\d+\\},\\sLandroid/app/Activity;->getWindow\\(\\)Landroid/view/Window;\\n(?:\\s*(?:[.#][^\\n]*)?\\n)*\\s*move-result-object\\s([pv]\\d+)(?:(?!\\.end\\smethod)[\\s\\S])*?invoke-virtual\\s\\{[pv]\\d+,\\s([pv]\\d+)(?:,\\s[pv]\\d+)?\\},\\sLandroid/view/Window;->(?:add|set)Flags\\(II?\\)V)(?:\\n\\n\\s*const/16\\s\\3,\\s0x2000\\n\\n\\s*invoke-virtual\\s\\{\\2,\\s\\3\\},\\sLandroid/view/Window;->clearFlags\\(I\\)V)?E",

            "$1\\n\\n\\tconst/16 $3, 0x2000\\n\\n\\tinvoke-virtual {$2, $3}, Landroid/view/Window;->clearFlags(I)V"
        },

        {
            "Screenshots Restriction Removal 3",

            "invoke-virtual (\\{.*\\}), Landroid/view/Window;->setFlags\\(II\\)V",

            "#$0"
        },

        {
            "Screenshots Restriction Removal 4",

            "invoke-virtual (\\{.*\\}), Landroid/view/Window;->addFlags\\(I\\)V",

            "#$0"
        },

        {
            "Screenshots Restriction Removal 5",

            "const/16 (.*), 0x2000\\s+invoke-virtual \\{(.*), (.*), (.*)\\}, Landroid/view/Window;->setFlags\\(II\\)V",

            "const/16 $1, 0x0\\n\\n    invoke-virtual {$2, $1, $1}, Landroid/view/Window;->setFlags(II)V"
        },

        {
            "Screenshots Restriction Removal 6",

            "Landroid/view/Window;->.*Flags",

            "const/16 (.*), 0x2000 const/16 $1, 0x0"
        },

        {
            "Secured Screen Restriction Patch",

            "(?:const/4\\s([pv]\\d+),\\s0x0\\n\\n\\s*)?(invoke-virtual\\s\\{(?:[pv]\\d{1,2}),\\s(\\1|(?:[pv]\\d+))\\},\\sLandroid/view/SurfaceView;->setSecure\\(Z\\)V)",

            "const/4 $3, 0x0\\n\\n\\t$2"
        },

        {
            "Patch to remove VPN Detection",

            "(const/4\\s([pv]\\d+),\\s0x4(?:(?!\\.end\\smethod)[\\s\\S])*?invoke-virtual\\s\\{[pv]\\d+,\\s\\2\\},\\sLandroid/net/NetworkCapabilities;->hasTransport\\(I\\)Z\\n(?:\\s*(?:[.#][^\\n]*)?\\n)*\\s{4}move-result\\s([pv]\\d+))(?:\\n\\n\\s*const/4\\s\\3,\\s0x0)?",

            "$1\\n\\n\\tconst/4 $3, 0x0"
        },

        {
            "Revenuecat Patch",

            "invoke-virtual \\{([pv]\\d+)\\}, Lcom/revenuecat/purchases/(CustomerInfo|PurchaserInfo);->getEntitlements\\(\\)Lcom/revenuecat/purchases/EntitlementInfos;([\\w\\W])*?move-result-object ([pv]\\d+)",

            "invoke-virtual \\{([pv]\\d+)\\}, Lcom/revenuecat/purchases/(CustomerInfo|PurchaserInfo);->getEntitlements\\(\\)Lcom/revenuecat/purchases/EntitlementInfos;([\\w\\W])*?move-result-object ([pv]\\d+)"
        },

        {
            "Patch to Fix Package Installer",

            "(invoke-virtual\\s\\{[pv]\\d+(?:,\\s[pv]\\d+)?\\},\\sLandroid/content/pm/(?:InstallSourceInfo|PackageManager);->getInstall(?:er|ing)PackageName\\((?:Ljava/lang/String;)?\\)Ljava/lang/String;\\n(?:\\s*(?:[.#][^\\n]*)?\\n)*\\s*move-result-object\\s([pv]\\d+))(?:\\n\\n\\s*const-string\\s\\2,\\s\".*\")?",

            "$1\\n\\n\\tconst-string $2, \"com.android.vending\""
        },

        {
            "Auto Rotation in App",

            "invoke-virtual\\s+\\{([^,]+),\\s*([vp][0-9]+)\\},\\s+Landroid/app/Activity;->setRequestedOrientation\\(I\\)V",

            "const/4 $2, 0x4\\ninvoke-virtual {$1, $2}, Landroid/app/Activity;->setRequestedOrientation(I)V"
        }
};


    // ============================================================
    // DP
    // ============================================================

    private int dp(float value) {
        return (int) (
                value *
                getResources().getDisplayMetrics().density
                + 0.5f
        );
    }


    // ============================================================
    // BASIC BACKGROUND
    // ============================================================

    private GradientDrawable bg(
            int color,
            int radius,
            int strokeColor,
            int strokeWidth) {

        GradientDrawable d =
                new GradientDrawable();

        d.setColor(color);
        d.setCornerRadius(dp(radius));

        if (strokeWidth > 0) {
            d.setStroke(
                    dp(strokeWidth),
                    strokeColor
            );
        }

        return d;
    }


    // ============================================================
    // TITLE
    // ============================================================

    private TextView title(String text) {

        TextView v =
                new TextView(this);

        v.setText(text);
        v.setTextSize(28);
        v.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );
        v.setTextColor(DARK);
        v.setGravity(Gravity.CENTER);

        return v;
    }


    // ============================================================
    // SECTION TITLE
    // ============================================================

    private TextView sectionTitle(String text) {

        TextView v =
                new TextView(this);

        v.setText(text);
        v.setTextSize(20);
        v.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        v.setTextColor(BLUE);

        v.setPadding(
                0,
                dp(8),
                0,
                dp(8)
        );

        return v;
    }


    // ============================================================
    // LABEL
    // ============================================================

    private TextView label(String text) {

        TextView v =
                new TextView(this);

        v.setText(text);
        v.setTextSize(15);
        v.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        v.setTextColor(DARK);

        v.setPadding(
                0,
                dp(8),
                0,
                dp(5)
        );

        return v;
    }


    // ============================================================
    // INPUT
    // ============================================================

    private EditText input(String hint) {

        EditText e =
                new EditText(this);

        e.setTextSize(16);
        e.setTextColor(Color.BLACK);
        e.setHintTextColor(GRAY);
        e.setHint(hint);

        e.setPadding(
                dp(14),
                dp(12),
                dp(14),
                dp(12)
        );

        e.setSingleLine(false);

        e.setBackground(
                bg(
                        LIGHT,
                        12,
                        BORDER,
                        2
                )
        );

        return e;
    }


    // ============================================================
    // BUTTON
    // ============================================================

    private Button button(String text) {

        Button b =
                new Button(this);

        b.setText(text);
        b.setTextSize(14);

        b.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        b.setTextColor(WHITE);
        b.setAllCaps(false);

        b.setBackground(
                bg(
                        BLUE,
                        12,
                        BLUE,
                        0
                )
        );

        return b;
    }


    // ============================================================
    // BACK BUTTON
    // ============================================================

    private Button backButton() {

        Button b =
                button("← Back");

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(50)
                );

        p.bottomMargin = dp(12);

        b.setLayoutParams(p);

        b.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        showHome();
                    }
                }
        );

        return b;
    }


    // ============================================================
    // SCROLL
    // ============================================================

    private ScrollView scroll(
            LinearLayout content) {

        ScrollView scroll =
                new ScrollView(this);

        scroll.setFillViewport(true);
        scroll.setBackgroundColor(WHITE);

        scroll.addView(content);

        return scroll;
    }


    // ============================================================
    // PAGE
    // ============================================================

    private LinearLayout page() {

        LinearLayout page =
                new LinearLayout(this);

        page.setOrientation(
                LinearLayout.VERTICAL
        );

        page.setPadding(
                dp(16),
                dp(16),
                dp(16),
                dp(30)
        );

        page.setBackgroundColor(WHITE);

        return page;
    }


    // ============================================================
    // SET PAGE
    // ============================================================

    private void setPage(View view) {

        root =
                new LinearLayout(this);

        root.setOrientation(
                LinearLayout.VERTICAL
        );

        root.setBackgroundColor(WHITE);

        root.addView(
                view,
                new LinearLayout.LayoutParams(
                        -1,
                        -1
                )
        );

        setContentView(root);
    }


    // ============================================================
    // ACTIVITY START
    // ============================================================

    @Override
    protected void onCreate(
            Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Window window = getWindow();

        window.setStatusBarColor(Color.BLACK);
        window.setNavigationBarColor(Color.BLACK);

        showHome();
    }


    // ============================================================
    // ANDROID BACK BUTTON
    // ============================================================

    @Override
    public void onBackPressed() {

        if (currentScreen != 0) {

            showHome();

        } else {

            super.onBackPressed();
        }
    }


    // ============================================================
    // HOME
    // ============================================================

    private void showHome() {

        currentScreen = 0;

        LinearLayout page = page();

        page.addView(
                title("✨ Smali Tools ✨"),
                new LinearLayout.LayoutParams(
                        -1,
                        dp(70)
                )
        );

        TextView sub =
                new TextView(this);

        sub.setText(
                "Smali regex, converters and patch tools"
        );

        sub.setTextSize(16);
        sub.setTextColor(GRAY);
        sub.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams sp =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(45)
                );

        sp.bottomMargin = dp(25);

        page.addView(sub, sp);


        // ========================================================
        // SMALI REGEX
        // ========================================================

        Button smali =
                button("Smali → Regex");

        page.addView(
                smali,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(60)
                )
        );

        smali.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        showSmaliRegex();
                    }
                }
        );


        // ========================================================
        // MORE CONVERTERS
        // ========================================================

        Button converters =
                button("More Converters");

        LinearLayout.LayoutParams cp =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(60)
                );

        cp.topMargin = dp(15);

        page.addView(
                converters,
                cp
        );

        converters.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        showConverters();
                    }
                }
        );


        // ========================================================
        // PATCHES
        // ========================================================

        Button patches =
                button("Patches");

        LinearLayout.LayoutParams pp =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(60)
                );

        pp.topMargin = dp(15);

        page.addView(
                patches,
                pp
        );

        patches.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        showPatches();
                    }
                }
        );

        setPage(scroll(page));
    }


    // ============================================================
    // SMALI → REGEX
    // ============================================================

    private void showSmaliRegex() {

        currentScreen = 1;

        LinearLayout page = page();

        page.addView(backButton());

        page.addView(
                title("Smali → Regex"),
                new LinearLayout.LayoutParams(
                        -1,
                        dp(65)
                )
        );

        TextView sub =
                new TextView(this);

        sub.setText(
                "Convert Smali code into a regex pattern"
        );

        sub.setTextSize(16);
        sub.setTextColor(GRAY);
        sub.setGravity(Gravity.CENTER);

        page.addView(
                sub,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(45)
                )
        );

        page.addView(
                label("Smali code")
        );

        final EditText smaliInput =
                input(
                        "Enter your Smali code here..."
                );

        smaliInput.setMinLines(6);

        page.addView(
                smaliInput,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(170)
                )
        );

        Button convert =
                button("SMALI → REGEX");

        LinearLayout.LayoutParams bp =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(55)
                );

        bp.topMargin = dp(15);

        page.addView(convert, bp);

        page.addView(
                label(
                        "Regex output — tap to copy"
                )
        );

        final TextView output =
                resultView();

        output.setText(
                "Tap the button above"
        );

        page.addView(
                output,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(200)
                )
        );

        convert.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        String value =
                                smaliInput
                                        .getText()
                                        .toString()
                                        .trim();

                        if (value.length() == 0) {

                            output.setText(
                                    "Type something above 👆"
                            );

                            return;
                        }

                        output.setText(
                                smaliRegex(value)
                        );
                    }
                }
        );

        makeCopyable(
                output,
                "Regex"
        );

        setPage(scroll(page));
    }


    // ============================================================
    // SMALI REGEX ENGINE
    // ============================================================

    private String smaliRegex(String input) {

        StringBuilder escaped =
                new StringBuilder();

        for (int i = 0; i < input.length(); i++) {

            char c = input.charAt(i);

            if ("\\.(){}[]+*^$?|".indexOf(c) >= 0) {
                escaped.append("\\");
            }

            escaped.append(c);
        }

        String result =
                escaped.toString();

        result = result.replaceAll(
                "\\b[vp]\\d+\\b",
                "([v|p]\\\\d+)"
        );

        result = result.replaceAll(
                "\\b\\d+\\b",
                "\\\\d+"
        );

        result = result.replaceAll(
                "\\s+",
                "\\\\s*"
        );

        return result;
    }


    // ============================================================
    // MORE CONVERTERS
    // ============================================================

    private void showConverters() {

        currentScreen = 2;

        LinearLayout page = page();

        page.addView(backButton());

        page.addView(
                title("More Converters"),
                new LinearLayout.LayoutParams(
                        -1,
                        dp(65)
                )
        );


        // ========================================================
        // TEXT ↔ BASE64
        // ========================================================

        page.addView(
                sectionTitle("Text ↔ Base64")
        );

        final EditText textBase64 =
                input("Text or Base64");

        textBase64.setMinLines(3);

        page.addView(
                textBase64,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(110)
                )
        );

        LinearLayout row1 =
                new LinearLayout(this);

        row1.setOrientation(
                LinearLayout.HORIZONTAL
        );

        Button encode =
                button("Text → Base64");

        Button decode =
                button("Base64 → Text");

        row1.addView(
                encode,
                new LinearLayout.LayoutParams(
                        0,
                        dp(55),
                        1
                )
        );

        LinearLayout.LayoutParams dlp =
                new LinearLayout.LayoutParams(
                        0,
                        dp(55),
                        1
                );

        dlp.leftMargin = dp(8);

        row1.addView(
                decode,
                dlp
        );

        page.addView(row1);

        final TextView textResult =
                resultView();

        page.addView(textResult);

        encode.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        try {

                            String s =
                                    textBase64
                                            .getText()
                                            .toString();

                            String r =
                                    Base64
                                            .encodeToString(
                                                    s.getBytes(
                                                            "UTF-8"
                                                    ),
                                                    Base64.NO_WRAP
                                            );

                            textResult.setText(r);

                        } catch (Exception e) {

                            textResult.setText(
                                    "Invalid input"
                            );
                        }
                    }
                }
        );

        decode.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        try {

                            String s =
                                    textBase64
                                            .getText()
                                            .toString()
                                            .trim();

                            byte[] data =
                                    Base64.decode(
                                            s,
                                            Base64.DEFAULT
                                    );

                            textResult.setText(
                                    new String(
                                            data,
                                            "UTF-8"
                                    )
                            );

                        } catch (Exception e) {

                            textResult.setText(
                                    "Invalid Base64"
                            );
                        }
                    }
                }
        );


        // ========================================================
        // DECIMAL ↔ HEX
        // ========================================================

        page.addView(
                sectionTitle("Decimal ↔ Hex")
        );

        final EditText decimalHex =
                input("Decimal or hexadecimal");

        page.addView(
                decimalHex,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(65)
                )
        );

        LinearLayout row2 =
                new LinearLayout(this);

        row2.setOrientation(
                LinearLayout.HORIZONTAL
        );

        Button decHex =
                button("Decimal → Hex");

        Button hexDec =
                button("Hex → Decimal");

        row2.addView(
                decHex,
                new LinearLayout.LayoutParams(
                        0,
                        dp(55),
                        1
                )
        );

        LinearLayout.LayoutParams hdp =
                new LinearLayout.LayoutParams(
                        0,
                        dp(55),
                        1
                );

        hdp.leftMargin = dp(8);

        row2.addView(
                hexDec,
                hdp
        );

        page.addView(row2);

        final TextView numberResult =
                resultView();

        page.addView(numberResult);

        decHex.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        try {

                            long n =
                                    Long.parseLong(
                                            decimalHex
                                                    .getText()
                                                    .toString()
                                                    .trim()
                                    );

                            numberResult.setText(
                                    Long.toHexString(n)
                                            .toUpperCase()
                            );

                        } catch (Exception e) {

                            numberResult.setText(
                                    "Invalid decimal"
                            );
                        }
                    }
                }
        );

        hexDec.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        try {

                            String s =
                                    decimalHex
                                            .getText()
                                            .toString()
                                            .trim();

                            if (s.startsWith("0x") ||
                                    s.startsWith("0X")) {

                                s = s.substring(2);
                            }

                            long n =
                                    Long.parseLong(
                                            s,
                                            16
                                    );

                            numberResult.setText(
                                    String.valueOf(n)
                            );

                        } catch (Exception e) {

                            numberResult.setText(
                                    "Invalid hexadecimal"
                            );
                        }
                    }
                }
        );


        // ========================================================
        // DECIMAL ↔ 0x HEX
        // ========================================================

        page.addView(
                sectionTitle(
                        "Decimal ↔ Hex (0x)"
                )
        );

        final EditText zeroHex =
                input("Decimal or 0x1234");

        page.addView(
                zeroHex,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(65)
                )
        );

        LinearLayout row3 =
                new LinearLayout(this);

        row3.setOrientation(
                LinearLayout.HORIZONTAL
        );

        Button to0x =
                button("Decimal → 0x");

        Button from0x =
                button("0x → Decimal");

        row3.addView(
                to0x,
                new LinearLayout.LayoutParams(
                        0,
                        dp(55),
                        1
                )
        );

        LinearLayout.LayoutParams fdp =
                new LinearLayout.LayoutParams(
                        0,
                        dp(55),
                        1
                );

        fdp.leftMargin = dp(8);

        row3.addView(
                from0x,
                fdp
        );

        page.addView(row3);

        final TextView zeroResult =
                resultView();

        page.addView(zeroResult);

        to0x.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        try {

                            long n =
                                    Long.parseLong(
                                            zeroHex
                                                    .getText()
                                                    .toString()
                                                    .trim()
                                    );

                            zeroResult.setText(
                                    "0x" +
                                    Long.toHexString(n)
                                            .toUpperCase()
                            );

                        } catch (Exception e) {

                            zeroResult.setText(
                                    "Invalid decimal"
                            );
                        }
                    }
                }
        );

        from0x.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        try {

                            String s =
                                    zeroHex
                                            .getText()
                                            .toString()
                                            .trim();

                            if (s.startsWith("0x") ||
                                    s.startsWith("0X")) {

                                s = s.substring(2);
                            }

                            long n =
                                    Long.parseLong(
                                            s,
                                            16
                                    );

                            zeroResult.setText(
                                    String.valueOf(n)
                            );

                        } catch (Exception e) {

                            zeroResult.setText(
                                    "Invalid hexadecimal"
                            );
                        }
                    }
                }
        );


        // ========================================================
        // DATE ↔ MILLISECONDS
        // ========================================================

        page.addView(
                sectionTitle(
                        "Date ↔ Milliseconds"
                )
        );

        final EditText dateMillis =
                input(
                        "UTC: yyyy-MM-dd HH:mm:ss"
                );

        page.addView(
                dateMillis,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(65)
                )
        );

        LinearLayout row4 =
                new LinearLayout(this);

        row4.setOrientation(
                LinearLayout.HORIZONTAL
        );

        Button dateToMs =
                button("Date → Millis");

        Button msToDate =
                button("Millis → Date");

        row4.addView(
                dateToMs,
                new LinearLayout.LayoutParams(
                        0,
                        dp(55),
                        1
                )
        );

        LinearLayout.LayoutParams mdlp =
                new LinearLayout.LayoutParams(
                        0,
                        dp(55),
                        1
                );

        mdlp.leftMargin = dp(8);

        row4.addView(
                msToDate,
                mdlp
        );

        page.addView(row4);

        final TextView dateResult =
                resultView();

        page.addView(dateResult);

        dateToMs.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        try {

                            Date d =
                                    utcFormat().parse(
                                            dateMillis
                                                    .getText()
                                                    .toString()
                                                    .trim()
                                    );

                            dateResult.setText(
                                    String.valueOf(
                                            d.getTime()
                                    )
                            );

                        } catch (Exception e) {

                            dateResult.setText(
                                    "Use: yyyy-MM-dd HH:mm:ss"
                            );
                        }
                    }
                }
        );

        msToDate.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        try {

                            long ms =
                                    Long.parseLong(
                                            dateMillis
                                                    .getText()
                                                    .toString()
                                                    .trim()
                                    );

                            dateResult.setText(
                                    utcFormat()
                                            .format(
                                                    new Date(ms)
                                            )
                            );

                        } catch (Exception e) {

                            dateResult.setText(
                                    "Invalid milliseconds"
                            );
                        }
                    }
                }
        );


        // ========================================================
        // DATE ↔ HEX
        // ========================================================

        page.addView(
                sectionTitle("Date ↔ Hex")
        );

        final EditText dateHex =
                input(
                        "UTC date or hexadecimal milliseconds"
                );

        page.addView(
                dateHex,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(65)
                )
        );

        LinearLayout row5 =
                new LinearLayout(this);

        row5.setOrientation(
                LinearLayout.HORIZONTAL
        );

        Button dateHexBtn =
                button("Date → Hex");

        Button hexDateBtn =
                button("Hex → Date");

        row5.addView(
                dateHexBtn,
                new LinearLayout.LayoutParams(
                        0,
                        dp(55),
                        1
                )
        );

        LinearLayout.LayoutParams xdp =
                new LinearLayout.LayoutParams(
                        0,
                        dp(55),
                        1
                );

        xdp.leftMargin = dp(8);

        row5.addView(
                hexDateBtn,
                xdp
        );

        page.addView(row5);

        final TextView dateHexResult =
                resultView();

        page.addView(dateHexResult);

        dateHexBtn.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        try {

                            Date d =
                                    utcFormat()
                                            .parse(
                                                    dateHex
                                                            .getText()
                                                            .toString()
                                                            .trim()
                                            );

                            dateHexResult.setText(
                                    Long.toHexString(
                                            d.getTime()
                                    ).toUpperCase()
                            );

                        } catch (Exception e) {

                            dateHexResult.setText(
                                    "Use: yyyy-MM-dd HH:mm:ss"
                            );
                        }
                    }
                }
        );

        hexDateBtn.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        try {

                            String s =
                                    dateHex
                                            .getText()
                                            .toString()
                                            .trim();

                            if (s.startsWith("0x") ||
                                    s.startsWith("0X")) {

                                s = s.substring(2);
                            }

                            long ms =
                                    Long.parseLong(
                                            s,
                                            16
                                    );

                            dateHexResult.setText(
                                    utcFormat()
                                            .format(
                                                    new Date(ms)
                                            )
                            );

                        } catch (Exception e) {

                            dateHexResult.setText(
                                    "Invalid hexadecimal"
                            );
                        }
                    }
                }
        );


        // ========================================================
        // COPYABLE RESULTS
        // ========================================================

        makeCopyable(
                textResult,
                "Converter"
        );

        makeCopyable(
                numberResult,
                "Number"
        );

        makeCopyable(
                zeroResult,
                "Number"
        );

        makeCopyable(
                dateResult,
                "Date"
        );

        makeCopyable(
                dateHexResult,
                "Date"
        );

        setPage(scroll(page));
    }


    // ============================================================
    // RESULT VIEW
    // ============================================================

    private TextView resultView() {

        TextView v =
                new TextView(this);

        v.setText("Result");
        v.setTextSize(16);
        v.setTextColor(Color.BLACK);
        v.setTypeface(
                Typeface.MONOSPACE
        );

        v.setTextIsSelectable(true);

        v.setGravity(
                Gravity.TOP | Gravity.START
        );

        v.setPadding(
                dp(14),
                dp(14),
                dp(14),
                dp(14)
        );

        v.setBackground(
                bg(
                        LIGHT,
                        12,
                        BORDER,
                        2
                )
        );

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(90)
                );

        p.topMargin = dp(10);

        v.setLayoutParams(p);

        return v;
    }


    // ============================================================
    // UTC FORMAT
    // ============================================================

    private SimpleDateFormat utcFormat() {

        SimpleDateFormat f =
                new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss",
                        Locale.US
                );

        f.setTimeZone(
                TimeZone.getTimeZone("UTC")
        );

        f.setLenient(false);

        return f;
    }


    // ============================================================
    // PATCHES
    //
    // PRE-BUILT FROM PATCHES ARRAY ABOVE
    // ============================================================

    private void showPatches() {

        currentScreen = 3;

        LinearLayout page = page();

        page.addView(backButton());

        page.addView(
                title("Patches"),
                new LinearLayout.LayoutParams(
                        -1,
                        dp(65)
                )
        );

        TextView description =
                new TextView(this);

        description.setText(
                "Pre-built Search / Replace patches"
        );

        description.setTextSize(16);
        description.setTextColor(GRAY);
        description.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams descParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(45)
                );

        descParams.bottomMargin = dp(15);

        page.addView(
                description,
                descParams
        );


        // ========================================================
        // ADD EVERY PATCH FROM THE SOURCE
        // ========================================================

        for (int i = 0; i < PATCHES.length; i++) {

            String name =
                    PATCHES[i][0];

            String search =
                    PATCHES[i][1];

            String replace =
                    PATCHES[i][2];

            addPatch(
                    page,
                    name,
                    search,
                    replace
            );
        }


        // ========================================================
        // IF NO PATCHES
        // ========================================================

        if (PATCHES.length == 0) {

            TextView empty =
                    new TextView(this);

            empty.setText(
                    "No patches added."
            );

            empty.setTextSize(16);
            empty.setTextColor(GRAY);
            empty.setGravity(Gravity.CENTER);

            page.addView(
                    empty,
                    new LinearLayout.LayoutParams(
                            -1,
                            dp(100)
                    )
            );
        }

        setPage(scroll(page));
    }


    // ============================================================
    // SINGLE PRE-BUILT PATCH CARD
    // ============================================================

    private void addPatch(
            LinearLayout container,
            String name,
            String searchText,
            String replaceText) {

        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.VERTICAL
        );

        card.setPadding(
                dp(16),
                dp(16),
                dp(16),
                dp(16)
        );

        card.setBackground(
                bg(
                        Color.rgb(252, 252, 252),
                        14,
                        Color.rgb(235, 235, 235),
                        2
                )
        );

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                );

        cardParams.bottomMargin =
                dp(15);

        container.addView(
                card,
                cardParams
        );


        // ========================================================
        // PATCH HEADER
        // ========================================================

        TextView header =
                new TextView(this);

        header.setText(name);

        header.setTextSize(21);

        header.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        header.setTextColor(
                Color.rgb(0, 170, 130)
        );

        card.addView(
                header,
                new LinearLayout.LayoutParams(
                        -1,
                        dp(45)
                )
        );


        // ========================================================
        // SEARCH
        // ========================================================

        card.addView(
                label("Search")
        );

        TextView search =
                patchText(searchText);

        card.addView(
                search,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );

        Button copySearch =
                button("COPY SEARCH");

        LinearLayout.LayoutParams searchButtonParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(50)
                );

        searchButtonParams.topMargin =
                dp(8);

        card.addView(
                copySearch,
                searchButtonParams
        );

        copySearch.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        copy(
                                search.getText()
                                        .toString()
                        );
                    }
                }
        );


        // ========================================================
        // REPLACE
        // ========================================================

        card.addView(
                label("Replace")
        );

        TextView replace =
                patchText(replaceText);

        card.addView(
                replace,
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                )
        );

        Button copyReplace =
                button("COPY REPLACE");

        LinearLayout.LayoutParams replaceButtonParams =
                new LinearLayout.LayoutParams(
                        -1,
                        dp(50)
                );

        replaceButtonParams.topMargin =
                dp(8);

        card.addView(
                copyReplace,
                replaceButtonParams
        );

        copyReplace.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        copy(
                                replace.getText()
                                        .toString()
                        );
                    }
                }
        );
    }


    // ============================================================
    // PATCH TEXT BOX
    // ============================================================

    private TextView patchText(String text) {

        TextView v =
                new TextView(this);

        v.setText(text);

        v.setTextSize(16);
        v.setTextColor(
                Color.rgb(180, 110, 45)
        );

        v.setTypeface(
                Typeface.MONOSPACE
        );

        v.setTextIsSelectable(true);

        v.setGravity(
                Gravity.TOP | Gravity.START
        );

        v.setPadding(
                dp(14),
                dp(14),
                dp(14),
                dp(14)
        );

        v.setBackground(
                bg(
                        LIGHT,
                        12,
                        BORDER,
                        2
                )
        );

        return v;
    }


    // ============================================================
    // COPYABLE RESULT
    // ============================================================

    private void makeCopyable(
            final TextView view,
            final String label) {

        view.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        String text =
                                view.getText()
                                        .toString();

                        if (text.length() > 0 &&
                                !text.equals("Result") &&
                                !text.equals(
                                        "Tap the button above"
                                )) {

                            copy(text);
                        }
                    }
                }
        );
    }


    // ============================================================
    // COPY
    // ============================================================

    private void copy(String text) {

        ClipboardManager clipboard =
                (ClipboardManager)
                        getSystemService(
                                Context.CLIPBOARD_SERVICE
                        );

        ClipData clip =
                ClipData.newPlainText(
                        "Copied",
                        text
                );

        clipboard.setPrimaryClip(clip);

        Toast.makeText(
                this,
                "Copied!",
                Toast.LENGTH_SHORT
        ).show();
    }
}