import React from 'react';
import {
	EuiFlexGroup,
	EuiFlexItem,
	EuiPageTemplate,
	EuiProvider,
	EuiSpacer,
	EuiTitle,
} from '@elastic/eui';
import { Header, headerHeight } from './Header';
import { euiThemeOverrides } from '../../constants/euiTheme';
import {
	Outlet,
	RouterProvider,
	createBrowserRouter,
	useMatch,
	useMatches,
} from 'react-router-dom';
import createCache from '@emotion/cache';
import { FeatureSwitchesProvider } from '../context/featureSwitches';
import { PageDataProvider } from '../../utils/window';
import styled from '@emotion/styled';
import { PageNotFound } from '../PageNotFound';
import { TagsTable } from '../TagsTable';
import { Breadcrumbs } from './Breadcrumbs';
import { Rules } from '../pages/Rules';

// Necessary while SASS and Emotion styles coexist within EUI.
const cache = createCache({
	key: 'eui',
	// Ensure SASS global styles override Emotion.
	prepend: true,
});

const PageContentContainer = styled.div`
	height: 100vh;
	padding: calc(${headerHeight} + 24px) 24px 24px 24px;
`;

const PageContent: React.FC = () => {
	const { name } = (useMatches()?.pop()?.handle || {}) as { name?: string };
	return (
		<EuiPageTemplate>
			<Header />
			<PageContentContainer>
				<EuiFlexGroup
					direction="column"
					gutterSize="none"
					style={{ height: '100%' }}
				>
					<EuiFlexItem grow={0}>
						<Breadcrumbs />
						<EuiSpacer size="s" />
						<EuiTitle>
							<h1>{name}</h1>
						</EuiTitle>
						<EuiSpacer size="m" />
					</EuiFlexItem>
					<EuiFlexItem style={{ overflow: 'hidden' }}>
						<Outlet />
					</EuiFlexItem>
				</EuiFlexGroup>
			</PageContentContainer>
		</EuiPageTemplate>
	);
};

const router = createBrowserRouter([
	{
		path: '/',
		handle: {
			name: 'Home',
		},
		element: <PageContent />,
		children: [
			{
				index: true,
				handle: {
					name: 'Search rules',
				},
				element: <Rules />,
			},
			{
				path: 'tags',
				handle: {
					name: 'Tags',
				},
				element: <TagsTable />,
			},
			{
				path: '*',
				handle: {
					name: 'Page not found',
				},
				element: <PageNotFound />,
			},
		],
	},
]);

export const Page = () => (
	<PageDataProvider>
		<FeatureSwitchesProvider>
			<EuiProvider modify={euiThemeOverrides} cache={cache}>
				<RouterProvider router={router} />
			</EuiProvider>
		</FeatureSwitchesProvider>
	</PageDataProvider>
);
