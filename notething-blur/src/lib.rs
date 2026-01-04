use std::os::raw::c_ulong;
use x11::xlib;
use std::ptr;

#[no_mangle]
pub extern "C" fn set_blur_x11(window_id: c_ulong, enable: i32) -> i32 {
    unsafe {
        let display = xlib::XOpenDisplay(ptr::null());
        if display.is_null() { return -1; }

        // 1. Atom cho KDE/KWin và Picom
        let kde_atom_name = std::ffi::CString::new("_KDE_NET_WM_BLUR_BEHIND_REGION").unwrap();
        let kde_blur_atom = xlib::XInternAtom(display, kde_atom_name.as_ptr(), xlib::False);

        // 2. Atom cho GNOME (Mutter)
        let gnome_atom_name = std::ffi::CString::new("_MUTTER_HINT_BLUR_BEHIND").unwrap();
        let gnome_blur_atom = xlib::XInternAtom(display, gnome_atom_name.as_ptr(), xlib::False);

        if enable != 0 {
            // --- BẬT BLUR ---
            // KDE/Picom: Giá trị [0] có nghĩa là mờ toàn bộ cửa sổ
            if kde_blur_atom != 0 {
                let data: [u32; 1] = [0];
                xlib::XChangeProperty(display, window_id, kde_blur_atom, xlib::XA_CARDINAL, 32,
                    xlib::PropModeReplace, data.as_ptr() as *const _, 0);
            }
            // GNOME: Một số phiên bản cần thuộc tính này để kích hoạt
            if gnome_blur_atom != 0 {
                let data: [u32; 1] = [1];
                xlib::XChangeProperty(display, window_id, gnome_blur_atom, xlib::XA_CARDINAL, 32,
                    xlib::PropModeReplace, data.as_ptr() as *const _, 1);
            }
        } else {
            // --- TẮT BLUR ---
            if kde_blur_atom != 0 { xlib::XDeleteProperty(display, window_id, kde_blur_atom); }
            if gnome_blur_atom != 0 { xlib::XDeleteProperty(display, window_id, gnome_blur_atom); }
        }

        xlib::XFlush(display);
        xlib::XCloseDisplay(display);
    }
    0
}