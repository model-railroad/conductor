import {
    GIT_BRANCH_STR,
    GIT_HASH_STR,
    GIT_LONG_STR,
    VERSION_MAJOR,
    VERSION_MINOR
} from "./GitBuild.ts";
import {Nav, Navbar} from "react-bootstrap";
import {Link, useLocation} from "react-router-dom";

function AppHeader() {
    const location = useLocation();

    const isActive = (path: string) => {
        if (path === "/") {
            return location.pathname === path;
        } else {
            return location.pathname.startsWith(path);
        }
    };

  return (
    <Navbar expand="md" bg="dark" data-bs-theme="dark" >
        <Navbar.Brand >
            Randall Trains Status
        </Navbar.Brand>
        <div className="flex-grow-1">
            <Nav className="nav-font-size flex-row justify-content-start justify-content-md-start" variant="underline">
                <Nav.Link as={Link} to="/"        active={isActive("/"       )}>Dashboard</Nav.Link>
                <Nav.Link as={Link} to="/history" active={isActive("/history")}>History</Nav.Link>
                <Nav.Link as={Link} to="/legacy"  active={isActive("/legacy" )}>Legacy</Nav.Link>
            </Nav>
        </div>
        <Navbar.Toggle aria-controls="text-nav-info" />
        <Navbar.Collapse id="text-nav-info" className="navbar-detail justify-content-end">
            <Navbar.Text>
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
  )
}

export default AppHeader
