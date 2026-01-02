package org;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

import javafx.stage.Stage;

public class GlassHelper {

    public static void enableBlur(Stage stage) {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            enableWindowsBlur(stage);
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            enableLinuxBlur(stage);
        }
    }

    // ================= WINDOWS IMPLEMENTATION =================

    private static void enableWindowsBlur(Stage stage) {
        javafx.application.Platform.runLater(() -> {
            try {
                String title = stage.getTitle();
                if (title == null || title.isEmpty()) return;

                WinDef.HWND hwnd = User32Wrapper.INSTANCE.FindWindow(null, title);
                
                if (hwnd != null) {
                    WindowCompositionAttributeData data = new WindowCompositionAttributeData();
                    data.Attribute = User32Wrapper.WindowCompositionAttribute.WCA_ACCENT_POLICY;
                    
                    AccentPolicy accent = new AccentPolicy();
                    accent.AccentState = User32Wrapper.AccentState.ACCENT_ENABLE_BLURBEHIND;
                    accent.AccentFlags = 0;
                    accent.GradientColor = 0; 
                    accent.AnimationId = 0;
                    accent.write();
                    
                    data.Data = accent.getPointer();
                    data.SizeOfData = accent.size();
                    
                    User32Wrapper.INSTANCE.SetWindowCompositionAttribute(hwnd, data);

                    try {
                        IntByReference preference = new IntByReference(2);
                        Dwmapi.INSTANCE.DwmSetWindowAttribute(hwnd, 33, preference, 4);
                    } catch (Exception e) {}

                    // Ngăn việc thêm nhiều listener nếu hàm này được gọi nhiều lần
                    if (stage.getProperties().get("blur_listeners_added") == null) {
                        javafx.beans.value.ChangeListener<Number> resizeListener = (obs, oldVal, newVal) -> {
                             updateWindowRegion(hwnd, stage);
                        };
                        stage.widthProperty().addListener(resizeListener);
                        stage.heightProperty().addListener(resizeListener);
                        stage.getProperties().put("blur_listeners_added", true);
                    }
                    
                    // Cập nhật lần đầu
                    updateWindowRegion(hwnd, stage);
                }
            } catch (Exception e) {
                // Ignore
            }
        });
    }

    private static void updateWindowRegion(WinDef.HWND hwnd, Stage stage) {
        try {
            double scaleX = stage.getOutputScaleX();
            double scaleY = stage.getOutputScaleY();
            
            double w = stage.getWidth();
            double h = stage.getHeight();

            if (w <= 0 || h <= 0) return;

            // Tính toán pixel thực
            int pixelW = (int) Math.ceil(w * scaleX);
            int pixelH = (int) Math.ceil(h * scaleY);
            
            int cornerRadius = (int) (7 * scaleX);

            // Gdi32 CreateRoundRectRgn:
            // nWidthEllipse và nHeightEllipse là ĐƯỜNG KÍNH của cung bo tròn.
            int diameter = 2 * cornerRadius;
            WinDef.HRGN rgn = Gdi32Wrapper.INSTANCE.CreateRoundRectRgn(0, 0, pixelW, pixelH, diameter, diameter);
            
            // bRedraw = true để vẽ lại cửa sổ ngay lập tức
            User32Wrapper.INSTANCE.SetWindowRgn(hwnd, rgn, true);
        } catch (Exception e) {}
    }

    private interface Dwmapi extends StdCallLibrary {
        Dwmapi INSTANCE = Native.load("dwmapi", Dwmapi.class, W32APIOptions.DEFAULT_OPTIONS);
        int DwmSetWindowAttribute(WinDef.HWND hwnd, int dwAttribute, IntByReference pvAttribute, int cbAttribute);
    }

    private interface Gdi32Wrapper extends StdCallLibrary {
        Gdi32Wrapper INSTANCE = Native.load("gdi32", Gdi32Wrapper.class, W32APIOptions.DEFAULT_OPTIONS);
        WinDef.HRGN CreateRoundRectRgn(int nLeftRect, int nTopRect, int nRightRect, int nBottomRect, int nWidthEllipse, int nHeightEllipse);
    }

    private interface User32Wrapper extends StdCallLibrary {
        User32Wrapper INSTANCE = Native.load("user32", User32Wrapper.class, W32APIOptions.DEFAULT_OPTIONS);

        WinDef.HWND FindWindow(String lpClassName, String lpWindowName);
        int SetWindowCompositionAttribute(WinDef.HWND hWnd, WindowCompositionAttributeData data);
        int SetWindowRgn(WinDef.HWND hWnd, WinDef.HRGN hRgn, boolean bRedraw);

        interface WindowCompositionAttribute {
            int WCA_ACCENT_POLICY = 19;
        }

        interface AccentState {
            int ACCENT_DISABLED = 0;
            int ACCENT_ENABLE_GRADIENT = 1;
            int ACCENT_ENABLE_TRANSPARENTGRADIENT = 2;
            int ACCENT_ENABLE_BLURBEHIND = 3;
            int ACCENT_ENABLE_ACRYLICBLURBEHIND = 4;
            int ACCENT_INVALID_STATE = 5;
        }
    }

    @Structure.FieldOrder({"Attribute", "Data", "SizeOfData"})
    public static class WindowCompositionAttributeData extends Structure {
        public int Attribute;
        public Pointer Data;
        public int SizeOfData;
    }

    @Structure.FieldOrder({"AccentState", "AccentFlags", "GradientColor", "AnimationId"})
    public static class AccentPolicy extends Structure {
        public int AccentState;
        public int AccentFlags;
        public int GradientColor;
        public int AnimationId;
    }

    // ================= LINUX (X11) IMPLEMENTATION =================

    private static void enableLinuxBlur(Stage stage) {
        javafx.application.Platform.runLater(() -> {
            try {
                String targetTitle = stage.getTitle();
                if (targetTitle == null || targetTitle.isEmpty()) return;

                X11 x11 = X11.INSTANCE;
                Pointer display = x11.XOpenDisplay(null);
                if (display == null) return;

                NativeLong root = x11.XDefaultRootWindow(display);
                NativeLong windowId = findWindowByTitle(x11, display, root, targetTitle);

                if (windowId != null) {
                    // Set _KDE_NET_WM_BLUR_BEHIND_REGION atom for KDE Plasma
                    // Atom data is null to indicate full window blur
                    NativeLong atom = x11.XInternAtom(display, "_KDE_NET_WM_BLUR_BEHIND_REGION", false);
                    if (atom.longValue() != 0) {
                        // Creating a property with 0 items effectively clears/sets region.
                        // For KDE, usually creating the property is enough, or setting it to empty list.
                        // To clear effect: XDeleteProperty. To set: ChangeProperty.
                        // We set it to cardinality 0 to mean "Whole Window" or just empty region?
                        // Actually KDE expects a list of rectangles. Empty list might mean nothing.
                        // But setting it usually triggers the effect. 
                        // Let's try passing 0 data to signify "no region masked out" -> blur everything ??
                        // Correct usage: A list of cardinality-4 integers (rectangles).
                        // If we want generic blur, usually just Requesting it via _NET_WM_WINDOW_TYPE logic or
                        // just the existence of the property.
                        // Let's try setting a dummy value of 0.
                        
                        int[] data = new int[]{0}; 
                        // CARDINAL format=32
                        x11.XChangeProperty(display, windowId, atom, x11.XInternAtom(display, "CARDINAL", false), 
                                            32, X11.PropModeReplace, data, 0); // nitems = 0 -> empty region?
                        // Note: Some docs say empty region = no blur.
                        // Ideally we should not set specific region to mean "default behavior"? 
                        // Let's trying setting nitems=0.
                        
                        // Also try to set _NET_WM_WINDOW_TYPE to normal just in case
                        // Not strictly blur related but good for compositors
                    }
                }
                
                x11.XFlush(display);
                x11.XCloseDisplay(display);

            } catch (Exception e) {
                // Ignore X11 errors
            }
        });
    }

    private static NativeLong findWindowByTitle(X11 x11, Pointer display, NativeLong window, String targetTitle) {
        // Check window title
        PointerByReference ptrName = new PointerByReference();
        if (x11.XFetchName(display, window, ptrName) != 0) {
            String name = ptrName.getValue().getString(0);
            x11.XFree(ptrName.getValue());
            if (targetTitle.equals(name)) {
                return window;
            }
        }

        // Recursively check children
        NativeLongByReference rootRet = new NativeLongByReference();
        NativeLongByReference parentRet = new NativeLongByReference();
        PointerByReference childrenRet = new PointerByReference();
        IntByReference nChildrenRet = new IntByReference();

        if (x11.XQueryTree(display, window, rootRet, parentRet, childrenRet, nChildrenRet) != 0) {
            int nChildren = nChildrenRet.getValue();
            if (nChildren > 0) {
                Pointer childrenPtr = childrenRet.getValue();
                long[] ids;
                if (Native.LONG_SIZE == 8) {
                    ids = childrenPtr.getLongArray(0, nChildren);
                } else {
                    // 32-bit systems? Unlikely today but safe to handle?
                    // JNA NativeLong size varies.
                    // For simplicity assume 64bit or let JNA handle data specific reading
                    // But manual array reading depends on size.
                    // Let's use getLongArray which reads 8 bytes? No, we need matching NativeLong size.
                    ids = new long[nChildren];
                    for(int i=0; i<nChildren; i++) {
                         // X11 Window ID is usually 32-bit (XID), but stored in 'Window' type which is unsigned long.
                         // On Linux 64-bit, long is 64-bit.
                         // On Linux 32-bit, long is 32-bit.
                         if (Native.LONG_SIZE == 8) 
                            ids[i] = childrenPtr.getLong(i * 8);
                         else
                            ids[i] = childrenPtr.getInt(i * 4);
                    }
                }
                
                // Free children list in X11
                x11.XFree(childrenRet.getValue());

                for (long id : ids) {
                    NativeLong childWin = new NativeLong(id);
                    NativeLong result = findWindowByTitle(x11, display, childWin, targetTitle);
                    if (result != null) return result;
                }
            }
        }
        return null;
    }

    private interface X11 extends Library {
        X11 INSTANCE = Native.load("X11", X11.class);

        Pointer XOpenDisplay(String display_name);
        NativeLong XDefaultRootWindow(Pointer display);
        int XCloseDisplay(Pointer display);
        int XFlush(Pointer display);
        
        int XQueryTree(Pointer display, NativeLong w, NativeLongByReference root_return, 
                       NativeLongByReference parent_return, PointerByReference children_return, 
                       IntByReference nchildren_return);
                       
        int XFetchName(Pointer display, NativeLong w, PointerByReference window_name_return);
        int XFree(Pointer data);
        
        NativeLong XInternAtom(Pointer display, String atom_name, boolean only_if_exists);
        
        int XChangeProperty(Pointer display, NativeLong w, NativeLong property, NativeLong type, 
                            int format, int mode, int[] data, int nelements); 
                            
        // Modes for XChangeProperty
        int PropModeReplace = 0;
    }
    
    // Helpers types for JNA X11
    public static class NativeLongByReference extends com.sun.jna.ptr.ByReference {
        public NativeLongByReference() { this(new NativeLong(0)); }
        public NativeLongByReference(NativeLong value) {
            super(Native.LONG_SIZE);
            setValue(value);
        }
        public void setValue(NativeLong value) {
            if (Native.LONG_SIZE == 4) getPointer().setInt(0, value.intValue());
            else getPointer().setLong(0, value.longValue());
        }
        public NativeLong getValue() {
            return new NativeLong(Native.LONG_SIZE == 4 ? getPointer().getInt(0) : getPointer().getLong(0));
        }
    }
}
