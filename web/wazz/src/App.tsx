import AppHeader from "./AppHeader.tsx";
import StatusViewer from "./StatusViewer.tsx";
import {HashRouter, Route, Routes} from "react-router-dom";
import LiveViewer from "./LiveViewer.tsx";
import PerfViewer from "./PerfViewer.tsx";

function App() {
  return (
    <HashRouter>
        <AppHeader/>
        <Routes>
            <Route path="/"         element={<LiveViewer    />} />
            <Route path="/perf"     element={<PerfViewer />} />
            <Route path="/legacy"   element={<StatusViewer  />} />
        </Routes>
    </HashRouter>
  )
}

export default App
