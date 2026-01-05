use std::os::raw::{c_ulong, c_int};
use x11::xlib;
use std::ptr;

// Khai báo các hằng số và hàm từ libXext (X11 Shape Extension)
// Vì crate x11 không bao gồm sẵn các binding này.
const SHAPE_BOUNDING: c_int = 0;
const SHAPE_CLIP: c_int = 1;
const SHAPE_SET: c_int = 0;

#[link(name = "Xext")]
extern "C" {
    fn XShapeCombineRegion(
        display: *mut xlib::Display,
        dest: xlib::Window,
        dest_kind: c_int,
        x_off: c_int,
        y_off: c_int,
        region: xlib::Region,
        op: c_int,
    );
}

#[no_mangle]
pub extern "C" fn set_blur_x11(window_id: c_ulong, enable: i32) -> i32 {
    unsafe {
        let display = xlib::XOpenDisplay(ptr::null());
        if display.is_null() { return -1; }

        let target_window = get_toplevel_window(display, window_id);

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

#[no_mangle]
pub extern "C" fn set_window_shape_rounded(window_id: c_ulong, width: c_int, height: c_int, radius: c_int) -> i32 {
    unsafe {
        let display = xlib::XOpenDisplay(ptr::null());
        if display.is_null() { return -1; }

        let target_window = get_toplevel_window(display, window_id);

        let region = xlib::XCreateRegion();
        
        let mut rect = xlib::XRectangle { x: radius as i16, y: 0, width: (width - 2 * radius) as u16, height: height as u16 };
        xlib::XUnionRectWithRegion(&mut rect, region, region);

        let mut rect_mid = xlib::XRectangle { x: 0, y: radius as i16, width: width as u16, height: (height - 2 * radius) as u16 };
        xlib::XUnionRectWithRegion(&mut rect_mid, region, region);

        apply_corner_arcs(region, width, height, radius);

        XShapeCombineRegion(display, target_window, SHAPE_BOUNDING, 0, 0, region, SHAPE_SET);
        XShapeCombineRegion(display, target_window, SHAPE_CLIP, 0, 0, region, SHAPE_SET);

        xlib::XDestroyRegion(region);
        xlib::XSync(display, xlib::False);
        xlib::XCloseDisplay(display);
    }
    0
}

unsafe fn get_toplevel_window(display: *mut xlib::Display, window: c_ulong) -> c_ulong {
    let mut root_return = 0;
    let mut parent_return = 0;
    let mut children_return: *mut xlib::Window = ptr::null_mut();
    let mut n_children_return = 0;

    let mut current = window;
    while xlib::XQueryTree(display, current, &mut root_return, &mut parent_return, &mut children_return, &mut n_children_return) != 0 {
        if !children_return.is_null() {
            xlib::XFree(children_return as *mut _);
            children_return = ptr::null_mut();
        }
        if parent_return == root_return || parent_return == 0 {
            return current;
        }
        current = parent_return;
    }
    window
}

unsafe fn apply_corner_arcs(region: xlib::Region, w: c_int, h: c_int, r: c_int) {
    for x in 0..r {
        let dy = (r as f64 - (r as f64 * r as f64 - (r - x) as f64 * (r - x) as f64).sqrt()) as i16;
        
        let mut r1 = xlib::XRectangle { x: x as i16, y: dy, width: 1, height: (r as i16 - dy) as u16 };
        xlib::XUnionRectWithRegion(&mut r1, region, region);

        let mut r2 = xlib::XRectangle { x: (w - x - 1) as i16, y: dy, width: 1, height: (r as i16 - dy) as u16 };
        xlib::XUnionRectWithRegion(&mut r2, region, region);

        let mut r3 = xlib::XRectangle { x: x as i16, y: (h - r) as i16, width: 1, height: (r as i16 - dy) as u16 };
        xlib::XUnionRectWithRegion(&mut r3, region, region);

        let mut r4 = xlib::XRectangle { x: (w - x - 1) as i16, y: (h - r) as i16, width: 1, height: (r as i16 - dy) as u16 };
        xlib::XUnionRectWithRegion(&mut r4, region, region);
    }
}