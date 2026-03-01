import { Outlet } from 'react-router-dom';
import AppNavbar from './Navbar';
import Sidebar from './Sidebar';

export default function Layout() {
    return (
        <div className="d-flex flex-column vh-100">
            <AppNavbar />
            <div className="d-flex flex-grow-1 overflow-hidden">
                <Sidebar />
                <main className="flex-grow-1 overflow-auto p-4 bg-white">
                    <Outlet />
                </main>
            </div>
        </div>
    );
}
