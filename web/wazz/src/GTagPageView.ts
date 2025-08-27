export function GTagPageView(title: string, location: string, path: string) {
    if (window.gtag) {
        const payload = {
            page_title: title,
            page_location: location,
            page_path: path,
        };
        window.gtag("event", "page_view", payload);
        console.log(`@@ DEBUG gtag payload: ${JSON.stringify(payload)}`);
    }
}

