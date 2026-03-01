import { Calendar, momentLocalizer } from 'react-big-calendar';
import moment from 'moment';
import 'react-big-calendar/lib/css/react-big-calendar.css';

const localizer = momentLocalizer(moment);

export default function CalendarPage() {
    const events = [];

    return (
        <div>
            <h3 className="mb-4">Calendar</h3>
            <p className="text-muted mb-3">
                Events and schedules will appear here once the module is active.
            </p>
            <div style={{ height: '600px' }}>
                <Calendar
                    localizer={localizer}
                    events={events}
                    startAccessor="start"
                    endAccessor="end"
                    views={['month', 'week', 'day']}
                    defaultView="month"
                    style={{ height: '100%' }}
                />
            </div>
        </div>
    );
}
