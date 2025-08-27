import AppHeader from "./AppHeader.tsx";
import StatusViewer from "./StatusViewer.tsx";
import {HashRouter, Route, Routes} from "react-router-dom";
import LiveViewer from "./LiveViewer.tsx";
import PerfViewer from "./PerfViewer.tsx";
import StatzViewer from "./StatzViewer.tsx";
import {GTagPageView} from "./GTagPageView.ts";

function App() {
  return (
    <HashRouter>
        <GTagPageView/>
        <AppHeader/>
        <Routes>
            <Route path="/"         element={<LiveViewer   />} />
            <Route path="/perf"     element={<PerfViewer   />} />
            <Route path="/statz"    element={<StatzViewer  />} />
            <Route path="/legacy"   element={<StatusViewer />} />
        </Routes>
    </HashRouter>
  )
}

export default App
