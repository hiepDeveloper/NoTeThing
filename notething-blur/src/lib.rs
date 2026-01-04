use std::os::raw::c_ulong;
use x11::xlib;
use std::ptr;

#[no_mangle]
pub extern "C" fn enable_blur_x11(window_id: c_ulong) -> i32 {
    unsafe {
        // 1. Mở kết nối với X Server
        let display = xlib::XOpenDisplay(ptr::null());
        if display.is_null() {
            return -1;
        }

        // 2. Tạo Atom cho hiệu ứng Blur của KDE/KWin (cũng được Picom hỗ trợ)
        let atom_name = std::ffi::CString::new("_KDE_NET_WM_BLUR_BEHIND_REGION").unwrap();
        let blur_atom = xlib::XInternAtom(display, atom_name.as_ptr(), xlib::False);

        if blur_atom != 0 {
            // Giá trị 0 ở đây thường ám chỉ áp dụng cho toàn bộ vùng cửa sổ
            let data: [u32; 1] = [0]; 
            
            xlib::XChangeProperty(
                display,
                window_id,
                blur_atom,
                xlib::XA_CARDINAL,
                32,
                xlib::PropModeReplace,
                data.as_ptr() as *const _,
                0, // Length 0 theo spec của KDE để kích hoạt toàn bộ
            );
        }

        // 3. Flush và đóng kết nối
        xlib::XFlush(display);
        xlib::XCloseDisplay(display);
    }
    0
}