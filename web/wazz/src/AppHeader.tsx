import {
    GIT_BRANCH_STR,
    GIT_HASH_STR,
    GIT_LONG_STR,
    VERSION_MAJOR,
    VERSION_MINOR
} from "./GitBuild.ts";
import {Navbar} from "react-bootstrap";

function AppHeader() {
  return (
    <>
        <Navbar bg="dark" data-bs-theme="dark" >
            <Navbar.Brand className="ms-3 pe-3">
                Randall Trains Status
            </Navbar.Brand>
            <Navbar.Collapse className="justify-content-end">
                <Navbar.Text className="navbar-detail">
                    <a href="https://www.alfray.com/trains/randall/" target="_blank">The Randall Museum Model Railroad</a>
                    <br/>
                    {`v${VERSION_MAJOR}.${VERSION_MINOR}, `} { ' ' }
                    <span className="gitinfo" title={GIT_LONG_STR}>
                        <a href={`https://github.com/model-railroad/conductor/tree/${GIT_BRANCH_STR}`} target="_blank">
                            build {GIT_BRANCH_STR}
                        </a>
                        { " @ " }
                        <a href={`https://github.com/model-railroad/conductor/commit/${GIT_HASH_STR}`} target="_blank">
                            {GIT_HASH_STR}
                        </a>
                    </span>
                </Navbar.Text>
            </Navbar.Collapse>
        </Navbar>
    </>
  )
}

export default AppHeader
