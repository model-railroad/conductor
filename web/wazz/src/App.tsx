import AppHeader from "./AppHeader.tsx";
import StatusViewer from "./StatusViewer.tsx";
import {HashRouter, Route, Routes} from "react-router-dom";
import LiveViewer from "./LiveViewer.tsx";
import HistoryViewer from "./HistoryViewer.tsx";

function App() {
  return (
    <HashRouter>
        <AppHeader/>
        <Routes>
            <Route path="/" element={<StatusViewer />} />
            <Route path="/live" element={<LiveViewer />} />
            <Route path="/history" element={<HistoryViewer />} />
        </Routes>
    </HashRouter>
  )
}

export default App
