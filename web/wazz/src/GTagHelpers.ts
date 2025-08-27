/**
 * Emit a gtag.js event.
 * - Recommended events: See https://developers.google.com/tag-platform/gtagjs/reference/events for a list.
 * - Custom events needs a bit more handling especially in GA4/Looker. See doc above if really needed.
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function GTagEvent(name: string, parameters: any) {
    if (window.gtag) {
        window.gtag("event", name, parameters);
        // console.log(`@@ DEBUG gtag ${name}: ${JSON.stringify(parameters)}`);
    }
}

/** Emit a gtag.js page_view. */
export function GTagPageView(title: string, location: string, path: string) {
    GTagEvent("page_view", {
        page_title: title,
        page_location: location,
        page_path: path,
    });
}

/**
 * Emit a gtag.js event when we refresh data.
 * We (ab)use "select content" as the Recommended event which is closest in meaning to "refresh content."
 * That avoids the burden of creating a custom event in GA4/Looker.
 */
export function GTagRefreshEvent(refreshKey: string) {
    GTagEvent("select_content", {
        content_type: "refresh",
        content_id: refreshKey })
}

