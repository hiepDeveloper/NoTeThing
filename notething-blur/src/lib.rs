use std::os::raw::c_ulong;
use x11::xlib;
use std::ptr;

#[no_mangle]
pub extern "C" fn set_blur_x11(window_id: c_ulong, enable: i32) -> i32 {
    unsafe {
        let display = xlib::XOpenDisplay(ptr::null());
        if display.is_null() { return -1; }

        // Tìm cửa sổ Top-level (Cơ chế quan trọng cho GNOME/Mutter)
        let mut target_window = window_id;
        let mut root_return = 0;
        let mut parent_return = 0;
        let mut children_return: *mut xlib::Window = ptr::null_mut();
        let mut n_children_return = 0;

        let mut current = window_id;
        // Leo lên cây cửa sổ để tìm cửa sổ cha cao nhất (ngay dưới Root)
        while xlib::XQueryTree(display, current, &mut root_return, &mut parent_return, &mut children_return, &mut n_children_return) != 0 {
            if !children_return.is_null() {
                xlib::XFree(children_return as *mut _);
                children_return = ptr::null_mut();
            }
            if parent_return == root_return || parent_return == 0 {
                target_window = current;
                break;
            }
            current = parent_return;
        }

        let kde_atom_name = std::ffi::CString::new("_KDE_NET_WM_BLUR_BEHIND_REGION").unwrap();
        let kde_blur_atom = xlib::XInternAtom(display, kde_atom_name.as_ptr(), xlib::False);

        let gnome_atom_name = std::ffi::CString::new("_MUTTER_HINT_BLUR_BEHIND").unwrap();
        let gnome_blur_atom = xlib::XInternAtom(display, gnome_atom_name.as_ptr(), xlib::False);

        if enable != 0 {
            if kde_blur_atom != 0 {
                let data: [u32; 1] = [0];
                xlib::XChangeProperty(display, target_window, kde_blur_atom, xlib::XA_CARDINAL, 32,
                    xlib::PropModeReplace, data.as_ptr() as *const _, 1);
            }
            if gnome_blur_atom != 0 {
                let data: [u32; 1] = [1];
                xlib::XChangeProperty(display, target_window, gnome_blur_atom, xlib::XA_CARDINAL, 32,
                    xlib::PropModeReplace, data.as_ptr() as *const _, 1);
            }
        } else {
            if kde_blur_atom != 0 { xlib::XDeleteProperty(display, target_window, kde_blur_atom); }
            if gnome_blur_atom != 0 { xlib::XDeleteProperty(display, target_window, gnome_blur_atom); }
        }

        xlib::XSync(display, xlib::False);
        xlib::XCloseDisplay(display);
    }
    0
}