import AppHeader from "./AppHeader.tsx";
import {HashRouter, Route, Routes} from "react-router-dom";
import LiveViewer from "./LiveViewer.tsx";
import PerfViewer from "./PerfViewer.tsx";
import StatzViewer from "./StatzViewer.tsx";

function App() {
    return (
        <HashRouter>
            <AppHeader/>
            <Routes>
                <Route path="/"         element={<LiveViewer   />} />
                <Route path="/perf"     element={<PerfViewer   />} />
                <Route path="/statz"    element={<StatzViewer  />} />
            </Routes>
        </HashRouter>
    )
}

export default App
