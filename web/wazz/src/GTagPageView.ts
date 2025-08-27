import {useLocation} from "react-router-dom";
import {useEffect} from "react";

export function GTagPageView() {
    const location = useLocation();

    useEffect(() => {
        if (window.gtag) {
            window.gtag('event', 'page_view', {
                page_title: document.title,
                page_location: window.location.href,
                page_path: location.pathname + location.search + location.hash,
            });
        }
    },[location]); // run effect when location changes

    return null;
}

