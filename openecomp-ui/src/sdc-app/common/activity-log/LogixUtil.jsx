import React, { Component } from 'react'; /* eslint-disable max-len */
const style =
    'LnJhYmJpdHt3aWR0aDo1ZW07aGVpZ2h0OjNlbTtiYWNrZ3JvdW5kOiM5OTk7Ym9yZGVyLXJhZGl1czo3MCUgOTAlIDYwJSA1MCU7cG9zaXRpb246cmVsYXRpdmU7LW1vei10cmFuc2Zvcm06cm90YXRlKDApIHRyYW5zbGF0ZSgtMmVtLDApOy1tcy10cmFuc2Zvcm06cm90YXRlKDApIHRyYW5zbGF0ZSgtMmVtLDApOy13ZWJraXQtdHJhbnNmb3JtOnJvdGF0ZSgwKSB0cmFuc2xhdGUoLTJlbSwwKTt0cmFuc2Zvcm06cm90YXRlKDApIHRyYW5zbGF0ZSgtMmVtLDApO2FuaW1hdGlvbjpob3AgMXMgaW5maW5pdGUgbGluZWFyO3otaW5kZXg6MX0ucmFiYml0OmFmdGVyLC5yYWJiaXQ6YmVmb3Jle2NvbnRlbnQ6IiI7cG9zaXRpb246YWJzb2x1dGU7YmFja2dyb3VuZDojZjFmMWYxfS5uby1mbGV4Ym94IC5yYWJiaXR7bWFyZ2luOjEwZW0gYXV0byAwfS5yYWJiaXQ6YmVmb3Jle3dpZHRoOjFlbTtoZWlnaHQ6MWVtO2JvcmRlci1yYWRpdXM6MTAwJTt0b3A6LjVlbTtsZWZ0Oi0uM2VtO2JveC1zaGFkb3c6NGVtIC40ZW0gMCAtLjM1ZW0gIzNmMzMzNCwuNWVtIDFlbSAwICNmMWYxZjEsNGVtIDFlbSAwIC0uM2VtICNmMWYxZjEsNGVtIDFlbSAwIC0uM2VtICNmMWYxZjEsNGVtIDFlbSAwIC0uNGVtICNmMWYxZjE7YW5pbWF0aW9uOmtpY2sgMXMgaW5maW5pdGUgbGluZWFyfS5yYWJiaXQ6YWZ0ZXJ7d2lkdGg6Ljc1ZW07aGVpZ2h0OjJlbTtib3JkZXItcmFkaXVzOjUwJSAxMDAlIDAgMDstbW96LXRyYW5zZm9ybTpyb3RhdGUoLTMwZGVnKTstbXMtdHJhbnNmb3JtOnJvdGF0ZSgtMzBkZWcpOy13ZWJraXQtdHJhbnNmb3JtOnJvdGF0ZSgtMzBkZWcpO3RyYW5zZm9ybTpyb3RhdGUoLTMwZGVnKTtyaWdodDoxZW07dG9wOi0xZW07Ym9yZGVyLXRvcDoxcHggc29saWQgI2Y3ZjVmNDtib3JkZXItbGVmdDoxcHggc29saWQgI2Y3ZjVmNDtib3gtc2hhZG93Oi0uNWVtIDAgMCAtLjFlbSAjZjFmMWYxfUBrZXlmcmFtZXMgaG9wezIwJXstbW96LXRyYW5zZm9ybTpyb3RhdGUoLTEwZGVnKSB0cmFuc2xhdGUoMWVtLC0yZW0pOy1tcy10cmFuc2Zvcm06cm90YXRlKC0xMGRlZykgdHJhbnNsYXRlKDFlbSwtMmVtKTstd2Via2l0LXRyYW5zZm9ybTpyb3RhdGUoLTEwZGVnKSB0cmFuc2xhdGUoMWVtLC0yZW0pO3RyYW5zZm9ybTpyb3RhdGUoLTEwZGVnKSB0cmFuc2xhdGUoMWVtLC0yZW0pfTQwJXstbW96LXRyYW5zZm9ybTpyb3RhdGUoMTBkZWcpIHRyYW5zbGF0ZSgzZW0sLTRlbSk7LW1zLXRyYW5zZm9ybTpyb3RhdGUoMTBkZWcpIHRyYW5zbGF0ZSgzZW0sLTRlbSk7LXdlYmtpdC10cmFuc2Zvcm06cm90YXRlKDEwZGVnKSB0cmFuc2xhdGUoM2VtLC00ZW0pO3RyYW5zZm9ybTpyb3RhdGUoMTBkZWcpIHRyYW5zbGF0ZSgzZW0sLTRlbSl9NjAlLDc1JXstbW96LXRyYW5zZm9ybTpyb3RhdGUoMCkgdHJhbnNsYXRlKDRlbSwwKTstbXMtdHJhbnNmb3JtOnJvdGF0ZSgwKSB0cmFuc2xhdGUoNGVtLDApOy13ZWJraXQtdHJhbnNmb3JtOnJvdGF0ZSgwKSB0cmFuc2xhdGUoNGVtLDApO3RyYW5zZm9ybTpyb3RhdGUoMCkgdHJhbnNsYXRlKDRlbSwwKX19QGtleWZyYW1lcyBraWNrezIwJSw1MCV7Ym94LXNoYWRvdzo0ZW0gLjRlbSAwIC0uMzVlbSAjM2YzMzM0LC41ZW0gMS41ZW0gMCAjZjFmMWYxLDRlbSAxLjc1ZW0gMCAtLjNlbSAjZjFmMWYxLDRlbSAxLjc1ZW0gMCAtLjNlbSAjZjFmMWYxLDRlbSAxLjllbSAwIC0uNGVtICNmMWYxZjF9NDAle2JveC1zaGFkb3c6NGVtIC40ZW0gMCAtLjM1ZW0gIzNmMzMzNCwuNWVtIDJlbSAwICNmMWYxZjEsNGVtIDEuNzVlbSAwIC0uM2VtICNmMWYxZjEsNC4yZW0gMS43NWVtIDAgLS4yZW0gI2YxZjFmMSw0LjRlbSAxLjllbSAwIC0uMmVtICNmMWYxZjF9fQ==';
/* eslint-enable max-len */
class LogixUtil extends Component {
    state = {
        whatToDisplay: false
    };

    componentWillReceiveProps(nextProps) {
        this.setState({
            whatToDisplay:
                window.btoa(nextProps.display) === 'YnJpdG5leSBiaXRjaCE='
        });
    }

    render() {
        if (this.state.whatToDisplay) {
            setTimeout(() => this.setState({ whatToDisplay: false }), 5000);
        }
        return (
            <div
                style={{
                    display: this.state.whatToDisplay ? 'block' : 'none',
                    position: 'fixed',
                    top: '50%',
                    left: '45%'
                }}>
                <style>{window.atob(style)}</style>
                <div className="rabbit" />
            </div>
        );
    }
}

export default LogixUtil;
